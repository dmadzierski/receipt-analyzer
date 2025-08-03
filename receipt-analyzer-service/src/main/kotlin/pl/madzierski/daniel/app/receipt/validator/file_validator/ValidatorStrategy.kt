package pl.madzierski.daniel.app.receipt.validator.file_validator

import org.springframework.web.multipart.MultipartFile

interface ValidatorStrategy {

    fun validate(file: MultipartFile): Boolean
}