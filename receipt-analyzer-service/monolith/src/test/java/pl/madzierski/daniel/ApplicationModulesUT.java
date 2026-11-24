package pl.madzierski.daniel;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModulesUT {

    @Test
    void modulesRespectTheirBoundaries() {
        ApplicationModules.of(ReceiptAnalyzerServiceApplication.class).verify();
    }
}
