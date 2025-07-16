package pl.madzierski.daniel

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Controller
@RestController
@RequestMapping("/users")
class UserController {

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal jwt: Jwt, authentication: Authentication): ResponseEntity<UserInfo> {
        val username = jwt.getClaimAsString("preferred_username")
        val email = jwt.getClaimAsString("email")


        return ResponseEntity.ok(UserInfo(username, email, authentication.authorities.map { it.authority }))
    }
}

class UserInfo(val username: String, val email: String, val all: List<String>)
