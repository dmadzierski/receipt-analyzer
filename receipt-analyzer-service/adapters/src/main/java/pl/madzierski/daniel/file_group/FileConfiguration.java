package pl.madzierski.daniel.file_group;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FileConfiguration {
    @Bean
    FileFacade fileFacade(final FileRepository fileRepository, final FileGroupRepository fileGroupRepository,
                          final FileTypeDetector fileTypeDetector, final FileStorage fileStorage,
                          @Value("${file.upload-dir}") final String uploadDir) {
        return new FileFacade(fileRepository, fileGroupRepository, fileTypeDetector, fileStorage, uploadDir);
    }
}
