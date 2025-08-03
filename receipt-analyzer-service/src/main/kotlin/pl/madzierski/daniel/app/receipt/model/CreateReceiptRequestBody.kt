package pl.madzierski.daniel.app.receipt.model

import jakarta.validation.constraints.NotEmpty

data class CreateReceiptRequestBody(
    @NotEmpty
    val name: String,
    val description: String?
)
