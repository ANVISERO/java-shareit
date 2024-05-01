package ru.practicum.shareit.request.util.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.util.mapper.CommentMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
public class CommentMapperTest {

    @InjectMocks
    CommentMapper commentMapper;

    static User user;

    @BeforeAll
    static void setUp() {
        user = ru.practicum.shareit.user.model.User.builder().id(1L).name("user").email("user@post.com").build();
    }

    @ParameterizedTest
    @MethodSource("provideItemsForCommentDtoToComment")
    @DisplayName("commentDtoToComment_whenInvoked_thenCommentReturned")
    void commentDtoToComment_whenInvoked_thenCommentReturned(Comment comment, CommentDto commentDto) {
        Comment convertedComment = commentMapper.commentDtoToComment(commentDto);

        assertNotNull(convertedComment);
        assertEquals(comment.getId(), convertedComment.getId());
        assertEquals(comment.getText(), convertedComment.getText());
        assertEquals(comment.getCreated(), convertedComment.getCreated());
    }

    @ParameterizedTest
    @MethodSource("provideItemsForCommentToCommentDto")
    @DisplayName("commentToCommentDto_whenInvoked_thenCommentDtoReturned")
    void commentToCommentDto_whenInvoked_thenCommentDtoReturned(Comment comment, CommentDto commentDto) {
        CommentDto convertedCommentDto = commentMapper.commentToCommentDto(comment);

        assertNotNull(convertedCommentDto);
        assertEquals(commentDto.getId(), convertedCommentDto.getId());
        assertEquals(commentDto.getText(), convertedCommentDto.getText());
        assertEquals(commentDto.getCreated(), convertedCommentDto.getCreated());
        assertEquals(commentDto.getAuthorName(), convertedCommentDto.getAuthorName());
    }

    private static Stream<Arguments> provideItemsForCommentDtoToComment() {
        LocalDateTime created = LocalDateTime.now();
        return Stream.of(
                arguments(
                        Comment.builder().id(1L).text("text").created(created).build(),
                        CommentDto.builder().id(1L).text("text").created(created).build(),
                        "Obvious comment"),
                arguments(
                        Comment.builder().text("text").created(created).build(),
                        CommentDto.builder().text("text").created(created).build(),
                        "Comment without id"),
                arguments(
                        Comment.builder().id(1L).created(created).build(),
                        CommentDto.builder().id(1L).created(created).build(),
                        "Comment without text"),
                arguments(
                        Comment.builder().id(1L).text("text").build(),
                        CommentDto.builder().id(1L).text("text").build(),
                        "Comment without created date"),
                arguments(
                        Comment.builder().build(),
                        CommentDto.builder().build(),
                        "Empty comment")
        );
    }

    private static Stream<Arguments> provideItemsForCommentToCommentDto() {
        LocalDateTime created = LocalDateTime.now();
        return Stream.of(
                arguments(
                        Comment.builder().id(1L).text("text").created(created).author(user).build(),
                        CommentDto.builder().id(1L).text("text").created(created).authorName("user").build(),
                        "Obvious comment"),
                arguments(
                        Comment.builder().text("text").created(created).author(user).build(),
                        CommentDto.builder().text("text").created(created).authorName("user").build(),
                        "Comment without id"),
                arguments(
                        Comment.builder().id(1L).created(created).author(user).build(),
                        CommentDto.builder().id(1L).created(created).authorName("user").build(),
                        "Comment without text"),
                arguments(
                        Comment.builder().id(1L).text("text").author(user).build(),
                        CommentDto.builder().id(1L).text("text").authorName("user").build(),
                        "Comment without created date"),
                arguments(
                        Comment.builder().id(1L).text("text").created(created).build(),
                        CommentDto.builder().id(1L).text("text").created(created).build(),
                        "Comment without author name"),
                arguments(
                        Comment.builder().build(),
                        CommentDto.builder().build(),
                        "Empty comment")
        );
    }
}
