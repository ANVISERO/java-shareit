package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.util.mapper.UserMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private UserMapper userMapper;

    User user;
    UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder().name("user").email("user@post.com").build();
        userDto = UserDto.builder().name("user").email("user@post.com").build();
    }

    @Test
    @SneakyThrows
    @DisplayName("createUser_whenUserIsValid_thenSavedUserDtoReturned")
    void createUser_whenUserIsValid_thenSavedUserDtoReturned() {
        UserDto userDtoSaved = userDto.toBuilder().id(1L).build();
        when(userMapper.userDtoToUser(any(UserDto.class))).thenReturn(user);
        when(userService.createUser(any(User.class))).thenReturn(userDtoSaved);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoSaved.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoSaved.getName())))
                .andExpect(jsonPath("$.email", is(userDtoSaved.getEmail())));

        verify(userMapper, times(1)).userDtoToUser(any(UserDto.class));
        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("createUser_whenUserWithoutName_thenBadRequestReturned")
    void createUser_whenUserWithoutName_thenBadRequestReturned() {
        userDto.setName(null);
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userMapper, never()).userDtoToUser(any(UserDto.class));
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("createUser_whenUserWithoutEmail_thenBadRequestReturned")
    void createUser_whenUserWithoutEmail_thenBadRequestReturned() {
        userDto.setEmail(null);
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userMapper, never()).userDtoToUser(any(UserDto.class));
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("createUser_whenUserNameIsTooBig_thenBadRequestReturned")
    void createUser_whenUserNameIsTooBig_thenBadRequestReturned() {
        userDto.setName(Stream.generate(() -> "a").limit(256).collect(Collectors.joining()));
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userMapper, never()).userDtoToUser(any(UserDto.class));
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("createUser_whenUserEmailIsTooBig_thenBadRequestReturned")
    void createUser_whenUserEmailIsTooBig_thenBadRequestReturned() {
        userDto.setEmail(Stream.generate(() -> "a").limit(256).collect(Collectors.joining()) + "@post.com");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userMapper, never()).userDtoToUser(any(UserDto.class));
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("createUser_whenUserEmailIncorrect_thenBadRequestReturned")
    void createUser_whenUserEmailIncorrect_thenBadRequestReturned() {
        userDto.setEmail("aaa");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userMapper, never()).userDtoToUser(any(UserDto.class));
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("createUser_whenUserWithId_thenBadRequestReturned")
    void createUser_whenUserWithId_thenBadRequestReturned() {
        userDto.setId(1L);
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userMapper, never()).userDtoToUser(any(UserDto.class));
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("updateUser_whenUserUpdatesAreValid_thenUpdatedUserDtoReturned")
    void updateUser_whenUserUpdatesAreValid_thenUpdatedUserDtoReturned() {
        UserDto userDtoUpdated = UserDto.builder().id(1L).name("updatedUser").email("updatedUser@post.com").build();
        User updates = User.builder().name("updatedUser").email("updatedUser@post.com").build();
        UserDto updatesDto = UserDto.builder().name("updatedUser").email("updatedUser@post.com").build();
        when(userMapper.userDtoToUser(any(UserDto.class))).thenReturn(updates);
        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(userDtoUpdated);

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .content(objectMapper.writeValueAsString(updatesDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoUpdated.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoUpdated.getName())))
                .andExpect(jsonPath("$.email", is(userDtoUpdated.getEmail())));

        verify(userMapper, times(1)).userDtoToUser(any(UserDto.class));
        verify(userService, times(1)).updateUser(anyLong(), any(User.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("getAllUsers_whenInvoked_thenListOfUserDtosReturned")
    void getAllUsers_whenInvoked_thenListOfUserDtosReturned() {
        userDto.setId(1L);
        UserDto userDto1 = userDto.toBuilder().id(2L).build();
        UserDto userDto2 = userDto.toBuilder().id(3L).build();
        List<UserDto> userDtos = List.of(userDto, userDto1, userDto2);
        when(userService.getAllUsers()).thenReturn(userDtos);

        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())))
                .andExpect(jsonPath("$[1].id", is(userDto1.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(userDto1.getName())))
                .andExpect(jsonPath("$[1].email", is(userDto1.getEmail())))
                .andExpect(jsonPath("$[2].id", is(userDto2.getId()), Long.class))
                .andExpect(jsonPath("$[2].name", is(userDto2.getName())))
                .andExpect(jsonPath("$[2].email", is(userDto2.getEmail())));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @SneakyThrows
    @DisplayName("getUserById_whenInvoked_thenUserDtoReturned")
    void getUserById_whenInvoked_thenUserDtoReturned() {
        userDto.setId(1L);
        when(userService.getUserById(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).getUserById(anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("getUserById_whenWithIncorrectUserId_thenBadRequestReturned")
    void getUserById_whenWithIncorrectUserId_thenUserDtoReturned() {
        mockMvc.perform(get("/users/{userId}","error")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("deleteUser_whenInvoked_thenResponseEntityWithOkReturned")
    void deleteUser_whenInvoked_thenResponseEntityWithOkReturned() {
        String response = mockMvc.perform(delete("/users/{userId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        verify(userService, times(1)).deleteUser(anyLong());
        assertEquals("User with id = 1 successfully deleted", response);
    }

    @Test
    @SneakyThrows
    @DisplayName("deleteUser_whenWithIncorrectUserId_thenBadRequestReturned")
    void deleteUser_whenWithIncorrectUserId_thenBadRequestReturned() {
        mockMvc.perform(delete("/users/{userId}", "error")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).deleteUser(anyLong());
    }
}
