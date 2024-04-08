package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.util.mapper.ItemRequestMapper;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestBody ItemRequestDto itemRequestDto,
                                            @RequestHeader(value = USER_HEADER) Long requestorId) {
        log.debug("POST request received to create a new item's request {} from user with id = {}",
                itemRequestDto, requestorId);
        ItemRequest itemRequest = itemRequestMapper.itemRequestDtoToItemRequest(itemRequestDto);
        log.debug("The message body converted to an object {}", itemRequest);
        return itemRequestService.createItemRequest(itemRequest, requestorId);
    }

    @GetMapping
    public List<ItemRequestInfoDto> getAllItemRequestsByRequestor(
            @RequestHeader(value = USER_HEADER) Long requestorId) {
        log.debug("GET request received to get all items' requests of the user with id = {}", requestorId);
        return itemRequestService.getAllItemRequestsByRequestor(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestInfoDto> getSeveralItemRequestsPaginated(
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestHeader(value = USER_HEADER) Long requestorId) {
        log.debug("GET request received from user with id = {} to get {} items' requests beginning with {} ",
                requestorId, size, from);
        return itemRequestService.getSeveralItemRequestsPaginated(from, size, requestorId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestInfoDto getItemRequestById(@PathVariable(name = "requestId") Long requestId,
                                                 @RequestHeader(value = USER_HEADER) Long ownerId) {
        log.debug("GET request received to get items' request by id = {} from user with id = {}", requestId, ownerId);
        return itemRequestService.getItemRequestById(requestId, ownerId);
    }
}
