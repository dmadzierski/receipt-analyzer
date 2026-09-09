package pl.madzierski.daniel.user;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserFacade {

    private final UserRepository userRepository;

    public void syncMe(String userSub) {
        if(userRepository.findUserByUserSub(userSub).isEmpty()) userRepository.save(new User(userSub));
    }
}
