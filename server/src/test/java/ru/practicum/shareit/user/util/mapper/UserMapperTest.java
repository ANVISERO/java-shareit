package ru.practicum.shareit.user.util.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {
    @InjectMocks
    UserMapper userMapper;


    @ParameterizedTest
    @MethodSource("provideUsers")
    @DisplayName("userDtoToUser_whenInvoked_thenUserReturned")
    void userDtoToUser_whenInvoked_thenUserReturned(User user, UserDto userDto) {
        User convertedUser = userMapper.userDtoToUser(userDto);

        assertNotNull(convertedUser);
        assertEquals(user.getId(), convertedUser.getId());
        assertEquals(user.getName(), convertedUser.getName());
        assertEquals(user.getEmail(), convertedUser.getEmail());
    }

    @ParameterizedTest
    @MethodSource("provideUsers")
    @DisplayName("userToUserDto_whenInvoked_thenUserDtoReturned")
    void userToUserDto_whenInvoked_thenUserDtoReturned(User user, UserDto userDto) {
        UserDto convertedUser = userMapper.userToUserDto(user);

        assertNotNull(convertedUser);
        assertEquals(userDto.getId(), convertedUser.getId());
        assertEquals(userDto.getName(), convertedUser.getName());
        assertEquals(userDto.getEmail(), convertedUser.getEmail());
    }

    @ParameterizedTest
    @MethodSource("provideUsersForUpdate")
    @DisplayName("updateUser_whenInvoked_thenUserReturned")
    void updateUser_whenInvoked_thenUserReturned(User existingUser, User user, User updatedUser) {
        User convertedUser = userMapper.updateUser(existingUser, user);

        assertNotNull(convertedUser);
        assertEquals(updatedUser.getId(), convertedUser.getId());
        assertEquals(updatedUser.getName(), convertedUser.getName());
        assertEquals(updatedUser.getEmail(), convertedUser.getEmail());
    }

    private static Stream<Arguments> provideUsers() {
        return Stream.of(
                arguments(
                        User.builder().id(1L).name("user").email("user@post.com").build(),
                        UserDto.builder().id(1L).name("user").email("user@post.com").build(),
                        "Obvious user"),
                arguments(
                        User.builder().name("user").email("user@post.com").build(),
                        UserDto.builder().name("user").email("user@post.com").build(),
                        "User without id"),
                arguments(
                        User.builder().id(1L).email("user@post.com").build(),
                        UserDto.builder().id(1L).email("user@post.com").build(),
                        "User without name"),
                arguments(
                        User.builder().id(1L).name("user").build(),
                        UserDto.builder().id(1L).name("user").build(),
                        "User without email"),
                arguments(
                        User.builder().build(),
                        UserDto.builder().build(),
                        "Empty user")

        );
    }

    private static Stream<Arguments> provideUsersForUpdate() {
        return Stream.of(
                arguments(
                        User.builder().id(1L).name("user").email("user@post.com").build(),
                        User.builder().id(1L).name("user").email("user@post.com").build(),
                        User.builder().id(1L).name("user").email("user@post.com").build(),
                        "Obvious users"),
                arguments(
                        User.builder().id(1L).name("user").email("user@post.com").build(),
                        User.builder().name("Updated user").build(),
                        User.builder().id(1L).name("Updated user").email("user@post.com").build(),
                        "Update name only"),
                arguments(
                        User.builder().id(1L).name("user").email("user@post.com").build(),
                        User.builder().id(1L).email("updatedUser@post.com").build(),
                        User.builder().id(1L).name("user").email("updatedUser@post.com").build(),
                        "Update email only"),
                arguments(
                        User.builder().id(1L).name("user").email("user@post.com").build(),
                        User.builder().build(),
                        User.builder().id(1L).name("user").email("user@post.com").build(),
                        "Update nothing"),
                arguments(
                        User.builder().id(1L).name("user").email("user@post.com").build(),
                        User.builder().name("updatedUser").email("updatedUser@post.com").build(),
                        User.builder().id(1L).name("updatedUser").email("updatedUser@post.com").build(),
                        "Update name and email")
        );
    }
}
