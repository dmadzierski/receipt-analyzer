package pl.madzierski.daniel.wallet.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class WalletDto {
    private UUID id;
    private String name;
    private LocalDateTime createdDate;
}
