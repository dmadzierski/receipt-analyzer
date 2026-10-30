package pl.madzierski.daniel.receipt.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReceiptQuery {
    private UUID id;
}
