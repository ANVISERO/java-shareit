package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.util.mapper.ItemRequestMapper;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;
import java.util.List;


@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;
    private final String userHeader = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto createItemRequest(@Validated({OnCreate.class, Default.class}) @RequestBody
                                            ItemRequestDto itemRequestDto,
                                            @RequestHeader(value = userHeader) @Positive @NotNull Long requestorId) {
        log.debug("POST request received to create a new item's request {} from user with id = {}",
                itemRequestDto, requestorId);
        ItemRequest itemRequest = itemRequestMapper.itemRequestDtoToItemRequest(itemRequestDto);
        log.debug("The message body converted to an object {}", itemRequest);
        return itemRequestService.createItemRequest(itemRequest, requestorId);
    }

    @GetMapping
    public List<ItemRequestInfoDto> getAllItemRequestsByRequestor(@RequestHeader(value = userHeader) @Positive @NotNull
                                                                  Long requestorId) {
        log.debug("GET request received to get all items' requests of the user with id = {}", requestorId);
        return itemRequestService.getAllItemRequestsByRequestor(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestInfoDto> getSeveralItemRequestsPaginated(
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size,
            @RequestHeader(value = userHeader) @Positive @NotNull Long requestorId) {
        log.debug("GET request received from user with id = {} to get {} items' requests beginning with {} ",
                requestorId, size, from);
        return itemRequestService.getSeveralItemRequestsPaginated(from, size, requestorId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestInfoDto getItemRequestById(@PathVariable(name = "requestId") @Positive Long requestId,
                                                 @RequestHeader(value = userHeader) @Positive @NotNull
                                                 Long ownerId) {
        log.debug("GET request received to get items' request by id = {} from user with id = {}", requestId, ownerId);
        return itemRequestService.getItemRequestById(requestId, ownerId);
    }
}
