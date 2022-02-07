package com.example.controllers;


import com.example.entity.User;
import com.example.entity.dto.UserDTO;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "User controller", description = "This controller is designed to get users and edit them")
@RestController
@RequestMapping(path ="/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

        private final UserService userService;

        @Autowired
        public UserController(UserService userService) {
                this.userService = userService;
        }

        @Operation(
                summary = "Create a new user (only for admin role)",
                description = "Allows you to create only new user"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping()
        @ResponseStatus(HttpStatus.CREATED)
        public User create(@RequestBody User user) {
            return userService.create(user);
        }

        @Operation(
                summary = "Get all users with param (only for admin role)",
                description = "Allows you to get all users with param"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping()
        public List<User> readAll(@Parameter(description = "Sorting direction - asc or desc (optional)") @RequestParam(name = "sorting_direction", required = false) String sortingDirection){
                return userService.findAllWithSortingDirection(sortingDirection);
        }

        @Operation(
                summary = "Get user by id (only for admin role)",
                description = "Allows you to get user by id"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/{id}")
        public User read(@PathVariable("id") Long id){
            return userService.findById(id);
        }

        @Operation(
                summary = "Get user profile (for user role)",
                description = "Allows you to get an existing user profile (for user role)"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping("/profile")
        public UserDTO readMyProfile(Principal principal){
                return userService.findMyProfile(principal);
        }

        @Operation(
                summary = "Update an existing user profile (for user role)",
                description = "Allows you to update an existing user profile (for user role)"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @PutMapping(path = "/profile")
        public UserDTO editProfile(@RequestBody UserDTO userDTO, Principal principal){
                return userService.editProfile(userDTO, principal);
        }

        @Operation(
                summary = "Update an existing user (only for admin role)",
                description = "Allows you to update an existing user"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping()
        public User update(@RequestBody User user){
                return userService.update(user);
        }

        @Operation(
                summary = "Delete an existing user by id (only for admin role)",
                description = "Allows you to delete an existing user by id"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
            userService.deleteById(id);
        }

}
