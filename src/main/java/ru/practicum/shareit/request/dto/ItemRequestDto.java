package ru.practicum.shareit.request.dto;


import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class ItemRequestDto {
    @Null(groups = OnCreate.class, message = "Id item's request must set automatically")
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Description of item's request can't be empty")
    @Size(max = 512, message = "Description of item's request must not exceed 255 characters")
    private String description;

    private LocalDateTime created;
}
