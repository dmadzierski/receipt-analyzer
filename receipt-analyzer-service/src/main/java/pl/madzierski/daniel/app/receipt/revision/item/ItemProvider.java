package pl.madzierski.daniel.app.receipt.revision.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ItemProvider {

    private final ItemRepository itemRepository;

    public List<ItemEntity> saveAll(Collection<ItemEntity> items) {
        return itemRepository.saveAll(items);
    }

    public Optional<ItemEntity> findById(String id) {
        return itemRepository.findById(id);
    }

    public ItemEntity save(ItemEntity item) {
        return itemRepository.save(item);
    }
}