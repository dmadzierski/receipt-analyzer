package pl.madzierski.daniel.receipt.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ReceiptItemQuery {
    private UUID id;
}
