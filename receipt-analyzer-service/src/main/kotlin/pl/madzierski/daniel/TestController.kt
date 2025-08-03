package pl.madzierski.daniel

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.madzierski.daniel.security.SecurityUtils


@Controller
@RestController
@RequestMapping("/users")
class UserController {

    @GetMapping("/me")
    fun me(): ResponseEntity<UserInfo> {
        return ResponseEntity.ok(UserInfo(SecurityUtils.getCurrentUserSub(), SecurityUtils.getCurrentUserRoles()))
    }
}

class UserInfo(val username: String, val all: List<String>)
