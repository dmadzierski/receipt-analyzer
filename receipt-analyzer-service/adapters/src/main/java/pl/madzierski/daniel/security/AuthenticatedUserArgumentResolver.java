package pl.madzierski.daniel.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.user.UserQueryRepository;
import pl.madzierski.daniel.user.model.AuthenticatedUser;

@Component
@RequiredArgsConstructor
class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserQueryRepository userQueryRepository;
    private final Cache<String, AuthenticatedUser> users = Caffeine.newBuilder()
        .build();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AuthenticatedUser.class.equals(parameter.getParameterType());
    }

    @Override
    public AuthenticatedUser resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                     NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.USER_NOT_FOUND);
        AuthenticatedUser user = users.get(token.getToken().getSubject(), sub -> userQueryRepository.findUser(sub)
            .map(found -> new AuthenticatedUser(found.id()))
            .orElse(null));
        if (user == null) throw new AppRuntimeException(AppRuntimeExceptionMessages.USER_NOT_FOUND);
        return user;
    }
}
