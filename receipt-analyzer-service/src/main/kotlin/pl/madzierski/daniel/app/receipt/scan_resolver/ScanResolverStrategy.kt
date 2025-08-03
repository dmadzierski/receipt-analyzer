package pl.madzierski.daniel.app.receipt.scan_resolver

import org.springframework.web.multipart.MultipartFile

interface ScanResolverStrategy {

    fun valid(file: MultipartFile);
}