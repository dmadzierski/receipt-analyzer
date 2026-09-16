package pl.madzierski.daniel.user;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.user.model.UserDto;

@Service
@AllArgsConstructor
public class UserFacade {

    private final UserRepository userRepository;

    public void syncMe(String userSub) {
        if (userRepository.findUserByUserSub(userSub).isEmpty()) userRepository.save(new User(userSub));
    }

    public UserDto getUser(String userSub) {
        return userRepository.findUserByUserSub(userSub)
            .map(it -> new UserDto(it.getId(), it.getUserSub()))
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.USER_NOT_FOUND));
    }
}
