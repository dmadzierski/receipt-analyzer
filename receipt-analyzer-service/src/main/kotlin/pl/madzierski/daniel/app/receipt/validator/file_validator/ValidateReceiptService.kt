package pl.madzierski.daniel.app.receipt.validator.file_validator

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages


@Service
class ValidateReceiptService(
    val pdfValidator: PdfValidator
) {

    fun validate(file: MultipartFile): Boolean {
        return when (file.contentType) {
            MediaType.APPLICATION_PDF_VALUE -> pdfValidator.validate(file)
            else -> throw AppRuntimeException(AppRuntimeExceptionMessages.UNHANDLED_MEDIA_TYPE)
        }

    }
}