package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;


@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<Object> createItemRequest(@Validated({OnCreate.class, Default.class}) @RequestBody
                                                    ItemRequestDto itemRequestDto,
                                                    @RequestHeader(value = USER_HEADER) @Positive @NotNull Long requestorId) {
        log.debug("POST request received to create a new item's request {} from user with id = {}",
                itemRequestDto, requestorId);
        return itemRequestClient.createItemRequest(itemRequestDto, requestorId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemRequestsByRequestor(@RequestHeader(value = USER_HEADER) @Positive @NotNull
                                                                Long requestorId) {
        log.debug("GET request received to get all items' requests of the user with id = {}", requestorId);
        return itemRequestClient.getAllItemRequestsByRequestor(requestorId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getSeveralItemRequestsPaginated(
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size,
            @RequestHeader(value = USER_HEADER) @Positive @NotNull Long requestorId) {
        log.debug("GET request received from user with id = {} to get {} items' requests beginning with {} ",
                requestorId, size, from);
        return itemRequestClient.getSeveralItemRequestsPaginated(from, size, requestorId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@PathVariable(name = "requestId") @Positive Long requestId,
                                                     @RequestHeader(value = USER_HEADER) @Positive @NotNull
                                                     Long ownerId) {
        log.debug("GET request received to get items' request by id = {} from user with id = {}", requestId, ownerId);
        return itemRequestClient.getItemRequestById(requestId, ownerId);
    }
}
