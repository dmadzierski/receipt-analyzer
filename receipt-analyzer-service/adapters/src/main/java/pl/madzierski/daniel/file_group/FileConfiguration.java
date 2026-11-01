package pl.madzierski.daniel.file_group;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.madzierski.daniel.receipt.ReceiptQueryRepository;

@Configuration
class FileConfiguration {
    @Bean
    FileFacade fileFacade(final FileRepository fileRepository,
                          final FileGroupQueryRepository fileGroupQueryRepository, final FileGroupRepository fileGroupRepository,
                          final FileTypeDetector fileTypeDetector, final ReceiptQueryRepository receiptQueryRepository,
                          @Value("${file.upload-dir}") final String uploadDir) {
        return new FileFacade(fileRepository, fileGroupQueryRepository, fileGroupRepository, new FileGroupFactory(new FileFactory()), fileTypeDetector,
            receiptQueryRepository, uploadDir);
    }
}
