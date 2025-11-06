package pl.madzierski.daniel.app.receipt.revision.item

import org.springframework.data.jpa.repository.JpaRepository

interface ItemRepository : JpaRepository<ItemEntity, String>