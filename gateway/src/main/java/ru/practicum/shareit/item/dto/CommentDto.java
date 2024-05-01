package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class CommentDto {
    @Null(groups = OnCreate.class, message = "Id of comment must set automatically")
    private Long id;

    @NotBlank(message = "Comment's content can't be empty")
    @Size(max = 512, message = "Text of the comment must not exceed 512 characters")
    private String text;

    @Size(max = 255, message = "Author's name of the comment must not exceed 255 characters")
    private String authorName;

    private LocalDateTime created;
}
