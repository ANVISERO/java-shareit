package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnMap;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    @Null(groups = OnCreate.class, message = "Id of comment must set automatically")
    @NotNull(groups = OnMap.class, message = "Comment's id can't be empty")
    @Positive(groups = OnMap.class, message = "Comment's id must be positive")
    private Long id;
    @NotBlank(message = "Comment's content can't be empty")
    @Size(max = 512, message = "Text of the comment must not exceed 512 characters")
    private String text;
    @NotBlank(groups = OnMap.class, message = "Comment's content can't be empty")
    @Size(max = 255, message = "Author's name of the comment must not exceed 255 characters")
    private String authorName;
    @NotNull(groups = OnMap.class, message = "Comment's created date can't be empty")
    private LocalDateTime created;
}
