package com.ppl.finalsaleweb.service;


import com.ppl.finalsaleweb.config.JwtUtil;
import com.ppl.finalsaleweb.model.User;
import com.ppl.finalsaleweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private JwtUtil jwtUtil;





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

        String encodedPassword = passwordEncoder.encode(user.getEmail().split("@")[0]);
        user.setPassword(encodedPassword);

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

    public ResponseEntity<?> loginUser(String email, String password) {
        try {
            // Fetch the user from MongoDB based on the provided email
            User user = userRepository.findByEmail(email);

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







}
