package pl.madzierski.daniel.readmodel.consistency;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pl.madzierski.daniel.readmodel.ReadModelProjector;

import java.time.Duration;

@Configuration
class ReadModelConsistencyConfiguration implements WebMvcConfigurer {

    private final ReadModelProjector projector;
    private final Duration timeout;

    ReadModelConsistencyConfiguration(ReadModelProjector projector,
                                      @Value("${read-model.consistency-timeout:PT3S}") Duration timeout) {
        this.projector = projector;
        this.timeout = timeout;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new ReadModelConsistencyInterceptor(projector, timeout));
    }
}
