package pl.madzierski.daniel.app.receipt.revision.item

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ItemService @Autowired constructor(
    private val itemRepository: ItemRepository
) {

    fun save(itemEntity: ItemEntity) = this.itemRepository.save(itemEntity)
}