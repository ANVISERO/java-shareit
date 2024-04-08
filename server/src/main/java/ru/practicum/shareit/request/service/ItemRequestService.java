package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(ItemRequest itemRequest, Long requestorId);

    List<ItemRequestInfoDto> getAllItemRequestsByRequestor(Long requestorId);

    List<ItemRequestInfoDto> getSeveralItemRequestsPaginated(int from, int size, Long requestorId);

    ItemRequestInfoDto getItemRequestById(Long requestId, Long ownerId);
}
