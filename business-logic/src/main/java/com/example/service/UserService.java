package com.example.service;

import com.example.entity.User;
import com.example.entity.dto.UserDTO;
import com.example.exceptions.BadRequestException;
import com.example.exceptions.NotFoundException;
import com.example.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final IUserRepository userRepository;

    @Autowired
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    
    public UserDTO convertToDTO(User user) {
        log.info("Method convertToDTO begin");
        return new UserDTO(
                user.getName(),
                user.getSurname(),
                user.getAddress(),
                user.getEmail());
    }

    public User convertFromDTO(UserDTO userDTO) {
        log.info("Method convertFromDTO begin");
        return new User(
                userDTO.getName(),
                userDTO.getSurname(),
                userDTO.getAddress(),
                userDTO.getEmail());
    }


    public User create(User user) {
        log.info("Method create begin");
        if (user.getId() == null || user.getId() != 0L) {
            User savedUser = userRepository.save(user);
            log.info("User" + savedUser.getId() + " is created");
            return savedUser;
        } else {
            IllegalArgumentException exception = new IllegalArgumentException("Error! New User id can only 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        }
    }
    
    public List<User> findAll() {
        log.info("Method findAll begin");
        List<User> users = userRepository.findAll();
        log.info("Method findAll result set size = " + users.size());
        return users;
    }

    public List<User> findAllWithSortingDirection(String sortingDirection) {
        log.info("Method findAllWithSortingDirection begin");
        Sort.Direction direction;
        try {
            if (sortingDirection == null || sortingDirection.isEmpty()) sortingDirection = "asc";
            direction = Sort.Direction.fromString(sortingDirection);
        } catch (IllegalArgumentException ex) {
            BadRequestException exception = new BadRequestException("Error! Invalid parameter sorting_direction: " + sortingDirection + ". Valid parameter: ASC, DESC.");
            log.error(exception.getMessage());
            throw exception;
        }
        Sort sort = Sort.by(direction, "name");
        List<User> users = userRepository.findAll(sort);
        log.info("Method findAll result set size = " + users.size());
        return users;
    }

    public User findById(Long id) {
        log.info("Method findById begin");
        return userRepository.findById(id).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! User not found with id " + id);
            log.error(exception.getMessage());
            return exception;
        });
    }

    public UserDTO findMyProfile(Principal principal) {
        log.info("Method findMyProfile begin");
        User user = userRepository.findByUsername(principal.getName()).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! User not found with username");
            log.error(exception.getMessage());
            return exception;
        });
        return convertToDTO(user);
    }

    public UserDTO editProfile(UserDTO userDTO, Principal principal) {
        log.info("Method editProfile begin for user " + principal.getName());
        User user = userRepository.findByUsername(principal.getName()).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! User not found with username");
            log.error(exception.getMessage());
            return exception;
        });
        user.setName(userDTO.getName());
        user.setSurname(userDTO.getSurname());
        user.setAddress(userDTO.getAddress());
        user.setEmail(userDTO.getEmail());
        User savedUser = userRepository.save(user);
        log.info("User " + principal.getName() + " is updated");
        return convertToDTO(savedUser);
    }

    public User update(User user) {
        log.info("Method update begin");
        idValidation(user.getId());
        User savedUser = userRepository.save(user);
        log.info("User " + savedUser.getId() + " is updated");
        return savedUser;
    }


    public void deleteById(Long id) {
        log.info("Method deleteById begin");
        idValidation(id);
        userRepository.deleteById(id);
        log.info("User " + id + " is deleted");
    }


    public void idValidation(Long id) {
        log.info("Method idValidation begin");
        if (id == null || id == 0L ) {
            IllegalArgumentException exception = new IllegalArgumentException("Error! User id cannot be 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        } else if (!userRepository.existsById(id)){
            IllegalArgumentException exception = new IllegalArgumentException("Error! User does not exist with this id - " + id);
            log.error(exception.getMessage());
            throw  exception;
        }
    }
}
