package com.example.service;

import com.example.entity.Role;
import com.example.entity.User;
import com.example.entity.dto.UserDTO;
import com.example.enums.ERole;
import com.example.repository.IUserRepository;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Principal principal;
    @Mock
    private IUserRepository userRepository;
    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;
    private List<User> users;

    @Captor
    ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void init(){
        user = new User();
        user.setId(1L);
        user.setName("Vasya");
        user.setSurname("Pupkin");
        user.setEmail("user@mail.com");
        user.setUsername("VasyaPupkin");
        user.setAddress("Tula");

        Role role = new Role();
        role.setId(1L);
        role.setName(ERole.ROLE_USER);
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(role);
        user.setRoles(roleSet);

        users = new ArrayList<>();
        users.add(user);

        userDTO = new UserDTO();
        userDTO.setName("Vasya");
        userDTO.setSurname("Pupkin");
        userDTO.setEmail("user@mail.com");
        userDTO.setAddress("Tula");
    }

    @AfterEach
    void reset(){
        Mockito.reset(userRepository);
    }


    @Test
    public void convertToDTOShouldConvertUserEntityToUserDTO(){
        UserDTO testUserDTO = userService.convertToDTO(user);

        assertThat(testUserDTO.getName(), equalTo(user.getName()));
        assertThat(testUserDTO.getSurname(), equalTo(user.getSurname()));
        assertThat(testUserDTO.getAddress(), equalTo(user.getAddress()));
        assertThat(testUserDTO.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    public void convertFromDTOShouldConvertUserDTOEntityToUser(){
        User testUser = userService.convertFromDTO(userDTO);

        assertThat(testUser.getName(), equalTo(userDTO.getName()));
        assertThat(testUser.getSurname(), equalTo(userDTO.getSurname()));
        assertThat(testUser.getAddress(), equalTo(userDTO.getAddress()));
        assertThat(testUser.getEmail(), equalTo(userDTO.getEmail()));
    }

    @Test
    public void createShouldCreateUser(){
        user.setId(null);
        when(userRepository.save(user)).thenReturn(user);

        userService.create(user);

        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId(), equalTo(null));
        assertThat(userCaptor.getValue().getName(), equalTo(user.getName()));
    }

    @Test
    public void findAllShouldCallStoreRepositoryMethodFindAll(){
        when(userRepository.findAll()).thenReturn(users);

        userService.findAll();

        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void findAllWithSortingDirectionShouldCallUserRepositoryMethodFindAllWithSort(){
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        when(userRepository.findAll(sort)).thenReturn(users);

        userService.findAllWithSortingDirection("asc");

        verify(userRepository, times(1)).findAll(sort);
    }

    @Test
    public void findByIdShouldCallStoreRepositoryMethodFindById(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.findById(1L);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void findMyProfileShouldReturnLoggedInUserProfile(){
        when(userRepository.findByUsername(principal.getName())).thenReturn(Optional.of(user));

        UserDTO testUserDTO = userService.findMyProfile(principal);

        verify(userRepository, times(1)).findByUsername(principal.getName());
    }

    @Test
    public void editMyProfileShouldUpdateLoggedInUserProfile(){
        when(userRepository.findByUsername(principal.getName())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDTO savedUserDTO = userService.editProfile(userDTO, principal);

        verify(userRepository, times(1)).findByUsername(principal.getName());
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId(), equalTo(user.getId()));
        assertThat(userCaptor.getValue().getName(), equalTo(user.getName()));
        assertThat(userCaptor.getValue().getSurname(), equalTo(user.getSurname()));
        assertThat(userCaptor.getValue().getAddress(), equalTo(user.getAddress()));
        assertThat(userCaptor.getValue().getEmail(), equalTo(user.getEmail()));
        assertThat(userCaptor.getValue().getRoles(), equalTo(user.getRoles()));

    }

    @Test
    public void updateShouldUpdateUser(){
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        userService.update(user);

        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId(), equalTo(user.getId()));
        assertThat(userCaptor.getValue().getName(), equalTo(user.getName()));
        assertThat(userCaptor.getValue().getSurname(), equalTo(user.getSurname()));
    }

    @Test
    public void deleteByIdShouldCallUserRepositoryMethodDeleteById(){
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void idValidationShouldThrowIllegalArgumentExceptionIfIdEqualsNullNotZero(){
        assertThatThrownBy(() -> userService.idValidation(null)).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> userService.idValidation(0L)).isInstanceOf(IllegalArgumentException.class);
    }

}
