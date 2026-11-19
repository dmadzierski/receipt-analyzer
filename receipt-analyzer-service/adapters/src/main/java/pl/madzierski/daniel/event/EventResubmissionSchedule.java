package pl.madzierski.daniel.event;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "read-model.resubmission.enabled", havingValue = "true", matchIfMissing = true)
class EventResubmissionSchedule {

    private static final Duration STALE_AFTER = Duration.ofSeconds(30);

    private final IncompleteEventPublications incompleteEventPublications;

    @Scheduled(fixedDelayString = "PT1M")
    void resubmitStalePublications() {
        incompleteEventPublications.resubmitIncompletePublicationsOlderThan(STALE_AFTER);
    }
}
