package pl.madzierski.daniel.user;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {

    private String id;
    private String userSub;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;


    public User(String userSub) {
        this.userSub = userSub;
    }
}
