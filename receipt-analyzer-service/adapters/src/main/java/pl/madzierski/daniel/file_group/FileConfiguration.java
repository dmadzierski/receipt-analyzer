package pl.madzierski.daniel.file_group;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FileConfiguration {
    @Bean
    FileFacade fileFacade(final FileRepository fileRepository,
                          final FileGroupQueryRepository fileGroupQueryRepository, final FileGroupRepository fileGroupRepository) {
        return new FileFacade(fileRepository, fileGroupQueryRepository, fileGroupRepository);
    }
}
