package pl.madzierski.daniel.file_group;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FileConfiguration {
    @Bean
    FileFacade fileFacade(final FileRepository fileRepository) {
        return new FileFacade(fileRepository);
    }
}
