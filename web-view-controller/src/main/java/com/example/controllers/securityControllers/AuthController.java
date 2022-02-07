package com.example.controllers.securityControllers;

import com.example.entity.User;
import com.example.springSecurity.SecurityService;
import com.example.springSecurity.pojo.JwtResponse;
import com.example.springSecurity.pojo.LoginRequest;
import com.example.springSecurity.pojo.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication controller", description = "This controller is designed to registration and authentication users")
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final SecurityService securityService;

    @Autowired
    public AuthController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Operation(
            summary = "Authentication end-point",
            description = "Allows you to authentication in api"
    )
    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    public JwtResponse authUser(@RequestBody LoginRequest loginRequest) {
        return securityService.authentication(loginRequest);
    }

    @Operation(
            summary = "Registration end-point",
            description = "Allows you to registration new users in api"
    )
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public User registerUser(@RequestBody SignupRequest signupRequest) {
        return securityService.registration(signupRequest);
    }
}
