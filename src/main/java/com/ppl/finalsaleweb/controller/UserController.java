package com.ppl.finalsaleweb.controller;

import com.ppl.finalsaleweb.Objects.ChangePasswordRequest;
import com.ppl.finalsaleweb.Objects.UserProfileUpdateRequest;
import com.ppl.finalsaleweb.config.JwtUtil;
import com.ppl.finalsaleweb.model.User;
import com.ppl.finalsaleweb.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.core.io.Resource;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;




    private static final String UPLOAD_DIR = "src/main/resources/uploads/profile_images";



    @GetMapping("/list")
    public ResponseEntity<?> getUserList() {
        return userService.getAllUsersExceptAdmin();
    }

    @GetMapping("/profile-image/{filename:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR).resolve(filename);
            if (!Files.exists(file) || !Files.isReadable(file)) {
                return ResponseEntity.notFound().build();
            }

            Resource fileSystemResource = new FileSystemResource(file);
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream"; // default content type
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileSystemResource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/link")
    public ResponseEntity<?> redirectToClient(@RequestParam(name = "link") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid link");
            }

            if (!jwtUtil.validateLinkToken(token)) {
                return ResponseEntity.status(HttpStatus.OK).body("Link expired or invalid");
            }

            URI redirectUri = new URI("http://localhost:3000/login");

            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(redirectUri).build();
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.OK).body("Link expired or invalid");
        } catch (URISyntaxException e) {
            return ResponseEntity.internalServerError().build();
        }
    }



    @GetMapping("/current-user")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractTokenFromAuthorizationHeader(authorizationHeader);
        return userService.getUser(token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        return userService.loginUser(username, password);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> verifyTokenRequest) {
        String token = verifyTokenRequest.get("token");
        return userService.verifyToken(token);
    }

    @PostMapping("/resend-email/{userId}")
    public ResponseEntity<?> resendEmail(@PathVariable String userId) {
        return userService.resendEmail(userId);
    }


    @PutMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(
            @PathVariable String id,
            @RequestBody ChangePasswordRequest changePasswordRequest) {
        String newPassword = changePasswordRequest.getNewPassword();
        boolean passwordChange = changePasswordRequest.isPasswordChange();

        return userService.changePassword(id, newPassword, passwordChange);
    }

    @PutMapping("/toggle-lock/{userId}")
    public ResponseEntity<?> toggleLock(@PathVariable String userId) {
        return userService.toggleLock(userId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        return userService.deleteUser(id);
    }

    @PatchMapping("/update-profile/{id}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable String id,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("profileImage") MultipartFile profileImage) {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setName(name);
        request.setEmail(email);
        request.setProfileImage(profileImage);



        // Call your service method and return the response
        return userService.updateUserProfile(id, request);
    }



    private String extractTokenFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

}
