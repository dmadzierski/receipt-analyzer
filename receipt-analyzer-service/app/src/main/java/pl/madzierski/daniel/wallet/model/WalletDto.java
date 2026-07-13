package pl.madzierski.daniel.wallet.model;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class WalletDto {
    private String id;
    private String name;
    private LocalDateTime createdDate;
}
