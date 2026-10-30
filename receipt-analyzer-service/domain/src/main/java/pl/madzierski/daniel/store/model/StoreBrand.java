package pl.madzierski.daniel.store.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class StoreBrand {

    private UUID id;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
