package pl.madzierski.daniel.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.product_dict.model.*;
import pl.madzierski.daniel.receipt.ReceiptFacade;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ProductDictFacade {

    private final ProductDictRepository productDictRepository;
    private final ProductDictQueryRepository productDictQueryRepository;
    private final ProductAliasRepository productAliasRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryQueryRepository productCategoryQueryRepository;
    private final ReceiptFacade receiptFacade;
    private final ProductMatcher productMatcher;

    public boolean allProductsExist(Collection<UUID> productDictIds) {
        return productDictIds.isEmpty() || productDictRepository.countByIdIn(productDictIds) == productDictIds.size();
    }

    public Optional<String> findProductName(UUID productDictId) {
        return productDictRepository.findById(productDictId).map(ProductDict::getName);
    }

    GetProductDictListResponse getProductDictList() {
        return new GetProductDictListResponse(productDictQueryRepository.findAllWithCategoryAndAliases());
    }

    @Transactional
    void updateProductDict(UpdateProductDictListRequest body) {
        body.items().forEach(item -> {
            List<UUID> dictIds = item.productDictList();
            UUID primaryId = dictIds.getFirst();
            List<UUID> sourceIds = dictIds.subList(1, dictIds.size());
            ProductDict product = productDictRepository.findById(primaryId)
                .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_NOT_FOUND));
            ensureVersion(product.getVersion(), item.version());
            String canonicalName = item.canonicalName().trim();
            updateAliases(product, canonicalName, item.aliasIds());
            product.setProductCategories(loadCategories(item.productCategoryIds()));
            productDictRepository.save(product);
            mergeProducts(primaryId, sourceIds);
        });
    }

    private static void ensureVersion(Long current, Long expected) {
        if (!Objects.equals(current, expected)) throw new AppRuntimeException(AppRuntimeExceptionMessages.VERSION_CONFLICT);
    }

    private void updateAliases(ProductDict product, String name, Set<UUID> aliasIds) {
        Set<ProductAlias> previous = new HashSet<>(product.getAliases());
        Set<ProductAlias> chosen = new HashSet<>(productAliasRepository.findAllByIds(aliasIds));

        boolean hasCanonical = chosen.stream().anyMatch(a -> a.getName().equalsIgnoreCase(name));
        if (!hasCanonical) {
            chosen.add(previous.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> ProductAlias.builder().name(name).build()));
        }
        product.setAliases(chosen);
        product.setName(name);
    }

    private void mergeProducts(UUID primaryId, List<UUID> sourceIds) {
        if (sourceIds.isEmpty()) return;
        receiptFacade.reassignProductDict(new ProductDictQuery(primaryId), sourceIds);
        productDictRepository.deleteAllByIdIn(sourceIds);
    }

    private Set<ProductCategory> loadCategories(Collection<UUID> ids) {
        Set<UUID> requested = ids == null ? Set.of() : new HashSet<>(ids);
        Set<ProductCategory> found = productCategoryRepository.findAllById(requested);
        if (found.size() != requested.size()) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND);
        }
        return found;
    }

    public Map<String, ProductDictQuery> findCanonicalNames(Set<String> aliases) {
        if (aliases.isEmpty()) return Map.of();
        List<ProductDict> products = productDictRepository.findAll();
        Map<String, ProductDictQuery> productsByAlias = new HashMap<>();
        aliases.stream()
            .filter(Objects::nonNull)
            .forEach(alias -> productMatcher.match(products, alias, null)
                .ifPresent(product -> productsByAlias.put(alias, new ProductDictQuery(product.getId()))));
        return productsByAlias;
    }

    @Transactional
    public Map<AliasToLearn, ProductDictQuery> learnAliases(Collection<AliasToLearn> aliasesToLearn) {
        List<ProductDict> products = new ArrayList<>(productDictRepository.findAll());
        Map<AliasToLearn, ProductDict> productsByAlias = new HashMap<>();
        aliasesToLearn.forEach(aliasToLearn -> {
            ProductDict product;
            if (aliasToLearn.productId() != null) product = productById(products, aliasToLearn.productId());
            else product = productMatcher.match(products, aliasToLearn.alias(), aliasToLearn.productName())
                .orElseGet(() -> {
                    ProductDict created = ProductDict.create(aliasToLearn.productName());
                    products.add(created);
                    return created;
                });
            product.addAlias(aliasToLearn.alias());
            productsByAlias.put(aliasToLearn, product);
        });
        Map<ProductDict, ProductDictQuery> savedProducts = new IdentityHashMap<>();
        productsByAlias.values().forEach(product -> savedProducts.computeIfAbsent(product,
            unsaved -> new ProductDictQuery(productDictRepository.save(unsaved).getId())));
        return productsByAlias.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> savedProducts.get(entry.getValue())));
    }

    private ProductDict productById(Collection<ProductDict> products, UUID productId) {
        return products.stream()
            .filter(product -> productId.equals(product.getId()))
            .findFirst()
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_NOT_FOUND));
    }

    GetProductCategoryListResponse getProductCategoryList() {
        List<GetProductCategoryListResponse.ProductCategory> items = productCategoryQueryRepository.findAllOrderedByName().stream()
            .map(category -> new GetProductCategoryListResponse.ProductCategory(category.getId(), category.getVersion(), category.getName()))
            .toList();
        return new GetProductCategoryListResponse(items);
    }

    @Transactional
    CreateProductCategoryResponse addProductCategory(CreateProductCategoryRequest request) {
        String name = request.name().trim();
        if (productCategoryRepository.existsByName(name))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
        ProductCategory savedProductCategory = productCategoryRepository.save(new ProductCategory(name));
        return new CreateProductCategoryResponse(savedProductCategory.getId());
    }

    @Transactional
    UpdateProductCategoryResponse updateProductCategory(UUID productCategoryId, UpdateProductCategoryRequest request) {
        ProductCategory productCategory = productCategoryRepository.findById(productCategoryId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
        ensureVersion(productCategory.getVersion(), request.version());
        String name = request.name().trim();

        if (!productCategory.getName().equals(name) && productCategoryRepository.existsByNameAndIdNot(name, productCategoryId)) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_ALREADY_EXISTS);
        }

        productCategory.setName(name);
        ProductCategory savedProductCategory = productCategoryRepository.save(productCategory);
        return new UpdateProductCategoryResponse(savedProductCategory.getId());
    }

    @Transactional
    void deleteProductCategory(UUID productCategoryId) {
        ProductCategory productCategory = productCategoryRepository.findById(productCategoryId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_NOT_FOUND));
        if (productDictRepository.isCategoryUsed(productCategory.getId())) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_CATEGORY_IN_USE);
        }
        productCategoryRepository.delete(productCategory);
    }

}
