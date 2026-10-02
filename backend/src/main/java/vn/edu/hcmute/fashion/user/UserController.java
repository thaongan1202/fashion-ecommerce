package vn.edu.hcmute.fashion.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public UserProfileResponse getProfile(@AuthenticationPrincipal Jwt jwt) {
        User user = getCurrentUser(jwt);
        return toProfileResponse(user);
    }

    @PutMapping
    public UserProfileResponse updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        User user = getCurrentUser(jwt);
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone().trim());

        return toProfileResponse(userRepository.save(user));
    }

    private User getCurrentUser(Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }
}