package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.util.mapper.UserMapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    UserMapper userMapper;
    @InjectMocks
    UserServiceImpl userService;

    User user1;
    UserDto userDto1;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).name("user1").email("user1@post.com").build();
        userDto1 = UserDto.builder().id(1L).name("user1").email("user1@post.com").build();
    }

    @Test
    @DisplayName("createUser_whenInvoked_thenSavedUserReturnedWithId")
    public void createUser_whenInvoked_thenSavedUserReturnedWithId() {
        User user = User.builder().name("user").email("user@post.com").build();
        UserDto userDtoCheck = userDto1.toBuilder().build();
        when(userRepository.save(any(User.class))).thenReturn(user1);
        when(userMapper.userToUserDto(any(User.class))).thenReturn(userDto1);

        UserDto createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals(userDtoCheck.getId(), createdUser.getId());
        assertEquals(userDtoCheck.getName(), createdUser.getName());
        assertEquals(userDtoCheck.getEmail(), createdUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).userToUserDto(any(User.class));
    }

    @Test
    @DisplayName("getAllUsers_whenInvoked_thenAllUsersReturned")
    public void getAllUsers_whenInvoked_thenAllUsersReturned() {
        User user2 = User.builder().id(2L).name("user2").email("user2@post.com").build();
        UserDto userDto2 = UserDto.builder().id(2L).name("user2").email("user2@post.com").build();
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.userToUserDto(any(User.class))).thenAnswer((invocationOnMock) -> {
            User user = invocationOnMock.getArgument(0);
            if (Objects.equals(user, user1)) {
                return userDto1;
            } else if (Objects.equals(user, user2)) {
                return userDto2;
            }
            return null;
        });

        List<UserDto> users = userService.getAllUsers();

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals(2, users.size());
        assertTrue(users.contains(userDto1.toBuilder().build()));
        assertTrue(users.contains(userDto2.toBuilder().build()));
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).userToUserDto(any(User.class));
    }

    @Test
    @DisplayName("getAllUsers_whenNoUsersExisted_thenEmptyListReturned")
    public void getAllUsers_whenNoUsersExist_thenEmptyListReturned() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> users = userService.getAllUsers();

        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(userRepository, times(1)).findAll();
        verify(userMapper, never()).userToUserDto(any(User.class));
    }

    @Test
    @DisplayName("getUserById_whenInvoked_thenUserReturnedById")
    public void getUserById_whenInvoked_thenUserReturnedById() {
        UserDto userDtoCheck = userDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userMapper.userToUserDto(any(User.class))).thenReturn(userDto1);

        UserDto user = userService.getUserById(1L);

        assertNotNull(user);
        assertEquals(userDtoCheck.getId(), user.getId());
        assertEquals(userDtoCheck.getName(), user.getName());
        assertEquals(userDtoCheck.getEmail(), user.getEmail());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, times(1)).userToUserDto(any(User.class));
    }

    @Test
    @DisplayName("getUserById_whenUserNotExisted_thenNotFoundExceptionThrown")
    public void getUserById_whenUserNotExisted_thenThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(999L));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, never()).userToUserDto(any(User.class));
    }

    @Test
    @DisplayName("updateUser_whenInvoked_thenUpdatedUserReturned")
    public void updateUser_whenInvoked_thenUpdatedUserReturned() {
        User user = User.builder().id(user1.getId()).name("updatedUser1").email("updatedUser1@post.com").build();
        User userToUpdate = User.builder().name("updatedUser1").email("updatedUser1@post.com").build();
        UserDto userDtoUpdated = userDto1.toBuilder().name("updatedUser1").email("updatedUser1@post.com").build();
        UserDto userDtoCheck = userDtoUpdated.toBuilder().name("updatedUser1").email("updatedUser1@post.com").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userMapper.updateUser(any(User.class), any(User.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToUserDto(any(User.class))).thenReturn(userDtoUpdated);

        UserDto updatedUser = userService.updateUser(user1.getId(), userToUpdate);

        assertNotNull(updatedUser);
        assertEquals(userDtoCheck.getId(), updatedUser.getId());
        assertEquals(userDtoCheck.getName(), updatedUser.getName());
        assertEquals(userDtoCheck.getEmail(), updatedUser.getEmail());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, times(1)).updateUser(any(User.class), any(User.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).userToUserDto(any(User.class));
    }

    @Test
    @DisplayName("updateUser_whenEmptyUser_thenUpdatedUserReturned")
    public void updateUser_whenEmptyUser_thenUpdatedUserReturned() {
        User userToUpdate = User.builder().build();
        UserDto userDtoCheck = userDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userMapper.updateUser(any(User.class), any(User.class))).thenReturn(user1);
        when(userRepository.save(any(User.class))).thenReturn(user1);
        when(userMapper.userToUserDto(any(User.class))).thenReturn(userDto1);

        UserDto updatedUser = userService.updateUser(user1.getId(), userToUpdate);

        assertNotNull(updatedUser);
        assertEquals(userDtoCheck.getId(), updatedUser.getId());
        assertEquals(userDtoCheck.getName(), updatedUser.getName());
        assertEquals(userDtoCheck.getEmail(), updatedUser.getEmail());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, times(1)).updateUser(any(User.class), any(User.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).userToUserDto(any(User.class));
    }

    @Test
    @DisplayName("getUserById_whenUserNotExisted_thenNotFoundExceptionThrown")
    public void updateUser_whenUserNotExisted_thenThrowNotFoundException() {
        User userToUpdate = User.builder().name("updatedUser1").email("updatedUser1@post.com").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(999L, userToUpdate));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, never()).updateUser(any(User.class), any(User.class));
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).userToUserDto(any(User.class));
    }

    @Test
    @DisplayName("deleteUser_whenInvoked_thenNothingReturned")
    public void deleteUser_whenInvoked_thenDeletedUserReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).deleteById(anyLong());
        verify(userMapper, never()).userToUserDto(any(User.class));
    }

    @Test
    @DisplayName("deleteUser_whenUserNotExisted_thenNotFoundExceptionThrown")
    public void deleteUser_whenUserNotExisted_thenThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.deleteUser(999L));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, never()).deleteById(anyLong());
    }
}
