package pl.madzierski.daniel

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Controller
@RestController(value = "/users")
class UserController {

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal user: OidcUser): Map<String, Any?> {
        return mapOf(
            "username" to user.preferredUsername,
            "email" to user.email,
            "claims" to user.claims
        )
    }
}
