package pl.madzierski.daniel.app.receipt.validator.file_validator

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class PdfValidator : ValidatorStrategy {

    override fun validate(file: MultipartFile): Boolean {
        return true
    }
}