package pl.madzierski.daniel.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.event.FileGroupChanged;
import pl.madzierski.daniel.file_group.event.FileGroupDeleted;
import pl.madzierski.daniel.product_dict.event.ProductCategoryChanged;
import pl.madzierski.daniel.product_dict.event.ProductCategoryDeleted;
import pl.madzierski.daniel.product_dict.event.ProductDictChanged;
import pl.madzierski.daniel.product_dict.event.ProductDictDeleted;
import pl.madzierski.daniel.receipt.event.ItemProductsReassigned;
import pl.madzierski.daniel.receipt.event.ReceiptChanged;
import pl.madzierski.daniel.receipt.event.ReceiptDeleted;
import pl.madzierski.daniel.receipt.event.RevisionChanged;
import pl.madzierski.daniel.receipt.event.RevisionDeleted;
import pl.madzierski.daniel.store.event.StoreBrandChanged;
import pl.madzierski.daniel.store.event.StoreChanged;
import pl.madzierski.daniel.wallet.event.WalletChanged;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventTypes {

    private static final Map<String, Class<? extends IntegrationEvent>> TYPES = Stream.<Class<? extends IntegrationEvent>>of(
            WalletChanged.class,
            StoreBrandChanged.class,
            StoreChanged.class,
            ProductCategoryChanged.class,
            ProductCategoryDeleted.class,
            ProductDictChanged.class,
            ProductDictDeleted.class,
            FileGroupChanged.class,
            FileGroupDeleted.class,
            ReceiptChanged.class,
            ReceiptDeleted.class,
            RevisionChanged.class,
            RevisionDeleted.class,
            ItemProductsReassigned.class)
        .collect(Collectors.toUnmodifiableMap(Class::getSimpleName, Function.identity()));

    public static String typeOf(IntegrationEvent event) {
        String type = event.getClass().getSimpleName();
        if (!TYPES.containsKey(type)) throw new AppRuntimeException(AppRuntimeExceptionMessages.EVENT_TYPE_UNKNOWN);
        return type;
    }

    public static Class<? extends IntegrationEvent> classOf(String type) {
        Class<? extends IntegrationEvent> eventClass = TYPES.get(type);
        if (eventClass == null) throw new AppRuntimeException(AppRuntimeExceptionMessages.EVENT_TYPE_UNKNOWN);
        return eventClass;
    }
}
