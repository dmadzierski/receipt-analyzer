package pl.madzierski.daniel.store.model;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class StoreBrand {

    private String id;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
