package pl.madzierski.daniel.user;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserFacade {

    private final UserRepository userRepository;

    void syncMe(String userSub) {
        if (userRepository.findUserByUserSub(userSub).isEmpty())
            userRepository.save(User.register(userSub));
    }
}
