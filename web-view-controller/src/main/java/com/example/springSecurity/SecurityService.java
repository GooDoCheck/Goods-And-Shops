package com.example.springSecurity;

import com.example.entity.Role;
import com.example.entity.User;
import com.example.enums.ERole;
import com.example.repository.IRoleRepository;
import com.example.service.UserService;
import com.example.springSecurity.jwt.JwtUtils;
import com.example.springSecurity.pojo.JwtResponse;
import com.example.springSecurity.pojo.LoginRequest;
import com.example.springSecurity.pojo.SignupRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SecurityService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final IRoleRepository roleRepository;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    public SecurityService(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, IRoleRepository roleRepository, UserService userService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    public JwtResponse authentication(LoginRequest loginRequest) {
        log.info("Method authentication begin");
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles);
    }

    public User registration(SignupRequest signupRequest) {
        log.info("Method registration begin");
        User user = new User(signupRequest.getName(),
                signupRequest.getSurname(),
                signupRequest.getAddress(),
                signupRequest.getEmail(),
                signupRequest.getUsername(),
                passwordEncoder.encode(signupRequest.getPassword()));
        Set<String> reqRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (reqRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER).get();
            roles.add(userRole);
        } else {
            reqRoles.forEach(r ->{
                String roleName = "ROLE_" + r.toUpperCase();

                ERole eRole = Arrays.stream(ERole.values())
                        .filter(role -> role.toString().equals(roleName))
                        .findFirst().orElse(ERole.ROLE_USER);
                Role userRole = roleRepository.findByName(eRole).orElseThrow(() -> {
                    IllegalArgumentException exception = new IllegalArgumentException("Error! Role name is incorrect " + r);
                    log.error(exception.getMessage());
                    throw exception;
                });
                roles.add(userRole);
            });
        }
        user.setRoles(roles);
        return userService.create(user);
    }
}
