package com.ppl.finalsaleweb.controller;
import com.ppl.finalsaleweb.model.User;
import com.ppl.finalsaleweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/list")
    public ResponseEntity<?> getUserList() {
        try {
            // Call the userService to get the list of users
            ResponseEntity<Map<String, Object>> usersResponse = userService.getAllUsersExceptAdmin();

            // Check the status code of the response and return it as-is
            if (usersResponse.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
                return usersResponse;
            } else {

                return ResponseEntity.status(usersResponse.getStatusCode()).body(usersResponse.getBody());
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        ResponseEntity<?> registrationResponse = userService.registerUser(user);

        if (registrationResponse.getStatusCode() == HttpStatus.CREATED) {
            return registrationResponse;
        } else if (registrationResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return registrationResponse;
        } else {
            return registrationResponse;
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        ResponseEntity<?> loginResponse = userService.loginUser(email, password);

        if (loginResponse.getStatusCode() == HttpStatus.OK) {
            return loginResponse;
        } else if (loginResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return loginResponse;
        } else {
            return loginResponse;
        }
    }

}
