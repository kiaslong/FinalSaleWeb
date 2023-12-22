package com.ppl.finalsaleweb.service;


import com.ppl.finalsaleweb.Objects.UserProfileUpdateRequest;
import com.ppl.finalsaleweb.config.JwtUtil;
import com.ppl.finalsaleweb.model.User;
import com.ppl.finalsaleweb.repository.UserRepository;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private JwtUtil jwtUtil;




    private static final String UPLOAD_DIR = "src/main/resources/uploads/profile_images";
    private final Logger logger = LoggerFactory.getLogger(UserService.class);





    public ResponseEntity<Map<String, Object>> getAllUsersExceptAdmin() {
        try {
            // Get all users from the repository
            List<User> allUsers = userRepository.findAll();

            // Filter out the admin users based on a condition (e.g., user email)
            List<User> nonAdminUsers = allUsers.stream()
                    .filter(user -> !user.getEmail().equals("admin@gmail.com"))
                    .collect(Collectors.toList());

            // Create a Map to represent the successful response
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Users retrieved successfully");
            response.put("data", nonAdminUsers);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            // Handle exceptions and return appropriate error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "Internal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    public ResponseEntity<?> registerUser(User user) {
        // Check if the email already exists in the repository
        boolean emailExists = userRepository.existsByEmail(user.getEmail());
        String username = user.getEmail().split("@")[0];

        String linkToken = jwtUtil.generateLink(user);

        UUID uuid = UUID.randomUUID();
        String hashed = sha256Hash(uuid.toString());
        String token = hashed.substring(0, 9);

        String encodedPassword = passwordEncoder.encode(user.getEmail().split("@")[0]);

        user.setPassword(encodedPassword);
        user.setUsername(username);
        user.setToken(token);
        String loginLink = "http://localhost:8080/users/link?link="+linkToken;


        String emailContent = createEmailContent(user.getFullname(), token, username,loginLink);
        EmailSender.sendEmail(user.getEmail(), "Your Login Token", emailContent);


        if (emailExists) {
            // Return a JSON response with a message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\": \"Email already exists. Please use a different email address.\"}");
        }

        try {
            User savedUser = userRepository.save(user);
            // Return a JSON response with a success message and user data
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("{\"message\": \"User registered successfully.\", \"user\": " + savedUser.getEmail() + "}");
        } catch (DataAccessException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Database Error: " + e.getMessage() + "\"}");
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<?> loginUser(String username, String password) {
        try {
            // Fetch the user from MongoDB based on the provided email
            User user = userRepository.findByUsername(username);

            if (user == null) {
                // User not found, return unauthorized response
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                errorResponse.put("message", "Authentication failed: User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Check if the entered password matches the stored hashed password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Passwords match, generate JWT token
                String token = jwtUtil.generateToken(user);

                // Create a response with the token
                Map<String, Object> response = new HashMap<>();
                response.put("status", HttpStatus.OK.value());
                response.put("message", "User authenticated successfully");
                response.put("token", token);

                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                // Passwords do not match, return unauthorized response
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                errorResponse.put("message", "Authentication failed: Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            // Handle authentication failure or other errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            errorResponse.put("message", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }



    public ResponseEntity<?> updateUser(String id, User updatedUser) {
        try {
            // Find the user by ID
            Optional<User> optionalUser = userRepository.findById(id);

            if (optionalUser.isEmpty()) {
                // User not found, return a not found response
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Update user properties
            User existingUser = optionalUser.get();
            existingUser.setFullname(updatedUser.getFullname());
            existingUser.setEmail(updatedUser.getEmail());

            // Save the updated user
            User savedUser = userRepository.save(existingUser);

            // Create a response with the updated user
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "User updated successfully");
            response.put("user", savedUser);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            // Handle the update failure or other errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public ResponseEntity<?> deleteUser(String id) {
        try {
            // Find the user by ID and delete it
            Optional<User> optionalUser = userRepository.findById(id);

            if (optionalUser.isEmpty()) {
                // User not found, return a not found response
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            User deletedUser = optionalUser.get();

            String existingProfilePhotoURL = deletedUser.getProfilePhotoURL();
            if (existingProfilePhotoURL != null && !existingProfilePhotoURL.isBlank()) {

                Path existingImagePath = Paths.get(UPLOAD_DIR);
                Path existingPath = existingImagePath.resolve(existingProfilePhotoURL);

                Files.deleteIfExists(existingPath);
            }

            userRepository.delete(deletedUser);

            // Create a response with the deleted user
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "User deleted successfully");
            response.put("deletedUser", deletedUser);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            // Handle the delete failure or other errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    public ResponseEntity<?> verifyToken(String token) {
        try {
            // Find the user by the token
            Optional<User> optionalUser = userRepository.findByToken(token);

            if (optionalUser.isEmpty()) {
                // Token not found, return a not found response
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                errorResponse.put("message", "Token not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            User user = optionalUser.get();

            // Return a JSON response with the user associated with the token
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Token found");
            response.put("user", user);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            // Handle token verification failure or other errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    public ResponseEntity<?> getUser(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token not provided");
            }

            String userId = jwtUtil.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            // Find the user by ID
            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // Return the user as JSON
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(optionalUser.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }

    public ResponseEntity<?> changePassword(String id, String newPassword, boolean passwordChange) {
        try {
            // Find the user by ID (assuming id is a String)
            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // Hash the new password
            String hashedPassword = passwordEncoder.encode(newPassword);

            // Set the new password and passwordChangeRequired flag
            user.setPassword(hashedPassword);
            user.setPasswordChangeRequired(passwordChange);

            // Save the updated user
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }


    public ResponseEntity<?> toggleLock(String userId) {
        try {
            // Find the user by ID (assuming userId is a String)
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // Toggle the isLock status
            user.setLock(!user.isLock());

            // Save the updated user
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK).body("Update Lock Status Successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }


    public ResponseEntity<?> updateUserProfile(String id, UserProfileUpdateRequest request) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);

            if (optionalUser.isEmpty()) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            User user = optionalUser.get();
            MultipartFile profileImage = request.getProfileImage();
            if (profileImage != null && !profileImage.isEmpty()) {
                String uniqueFilename = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);

                Files.createDirectories(uploadPath); // Ensure the directory exists
                Path imagePath = uploadPath.resolve(uniqueFilename);

                String existingProfilePhotoURL = user.getProfilePhotoURL();
                if (existingProfilePhotoURL != null && !existingProfilePhotoURL.isBlank()) {

                    Path existingImagePath = Paths.get(UPLOAD_DIR);
                    Path existingPath = existingImagePath.resolve(existingProfilePhotoURL);

                    Files.deleteIfExists(existingPath);
                }
                String filePath = imagePath.toFile().getCanonicalPath();

                profileImage.transferTo(new File(filePath));
                user.setProfilePhotoURL(uniqueFilename);


            }

            user.setFullname(request.getName());
            user.setEmail(request.getEmail());
            userRepository.save(user);


            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }


    public ResponseEntity<?> resendEmail(String userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new Exception("User not found"));

            String linkToken = jwtUtil.generateLink(user);
            String loginLink = "http://localhost:8080/users/link?link=" + linkToken;
            String emailContent = createEmailContent(user.getFullname(), user.getToken(), user.getUsername(), loginLink);

            EmailSender.sendEmail(user.getEmail(), "Your Login Token", emailContent);

            return ResponseEntity.ok().body("{\"message\": \"Email resent successfully.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Error: " + e.getMessage() + "\"}");
        }
    }



    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }



    private static String createEmailContent(String name, String token, String usernameFromEmail, String loginLink) {
        return "<p>Hello " + name + ",</p>" +
                "<p>Your login token is: " + token + "</p>" +
                "<p>Your username and password is: " + usernameFromEmail + ".</p>" +
                "<p>Please remember this token when login</p>" +
                "<p>Click the link below to login:</p>" +
                "<a href=\"" + loginLink + "\">" + "Login Link" + "</a>" +
                "<p>Note: This link will expire in 1 minute.</p>";
    }

}
