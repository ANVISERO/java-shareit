package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.util.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.util.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.ShareItPageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    public ItemRequestDto createItemRequest(ItemRequest itemRequest, Long requestorId) {
        User requester = userRepository.findById(requestorId).orElseThrow(() -> {
            log.warn("User with id {} not found", requestorId);
            return new NotFoundException(String.format("User with id %d not found", requestorId));
        });
        log.debug("User with id = {} found", requestorId);
        itemRequest.setRequestor(requester);
        return itemRequestMapper.itemRequestToItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestInfoDto> getAllItemRequestsByRequestor(Long requestorId) {
        User requester = userRepository.findById(requestorId).orElseThrow(() -> {
            log.warn("User with id {} not found", requestorId);
            return new NotFoundException(String.format("User with id %d not found", requestorId));
        });
        log.debug("User with id = {} found", requestorId);
        List<ItemRequest> itemRequests = itemRequestRepository.findRequestByRequestorIdOrderByCreatedDesc(requestorId);
        if (itemRequests.isEmpty()) {
            log.warn("User with id = {} hasn't got any item requests", requester.getId());
            return Collections.emptyList();
        }
        log.debug("Item requests: {}", itemRequests);
        List<Item> itemsResponse = itemRepository.findAllByItemRequestIn(itemRequests);
        log.debug("Items response: {}", itemsResponse);
        if (itemsResponse.isEmpty()) {
            log.warn("All requests have no responses");
            return itemRequests.stream().map((request) -> {
                ItemRequestInfoDto itemRequestInfoDto =
                        itemRequestMapper.itemRequestToItemRequestInfoDto(request);
                itemRequestInfoDto.setItems(Collections.emptyList());
                return itemRequestInfoDto;
            }).collect(Collectors.toList());
        } else {
            Map<Long, List<Item>> responses = itemsResponse.stream()
                    .collect(Collectors.groupingBy((item) -> item.getItemRequest().getId()));
            log.debug("Item responses: {}", responses);
            return itemRequests.stream().map((request) -> {
                ItemRequestInfoDto itemRequestInfoDto = itemRequestMapper.itemRequestToItemRequestInfoDto(request);
                itemRequestInfoDto.setItems(responses.get(request.getId()).stream()
                        .map(itemMapper::itemToItemRequestInfoDtoItemDto)
                        .collect(Collectors.toList()));
                return itemRequestInfoDto;
            }).collect(Collectors.toList());
        }
    }

    @Override
    public List<ItemRequestInfoDto> getSeveralItemRequestsPaginated(int from, int size, Long requestorId) {
        userRepository.findById(requestorId).orElseThrow(() -> {
            log.warn("User with id {} not found", requestorId);
            return new NotFoundException(String.format("User with id %d not found", requestorId));
        });
        log.debug("User with id = {} found", requestorId);
        ShareItPageRequest pageRequest = new ShareItPageRequest(from, size, Sort.by("created").descending());
        Page<ItemRequest> itemRequestPage = itemRequestRepository.findAllWithoutRequestor(requestorId, pageRequest);
        if (itemRequestPage.isEmpty()) {
            log.warn("Item requests not found");
            return Collections.emptyList();
        }
        List<ItemRequest> itemRequests = itemRequestPage.getContent();
        log.debug("Item requests: {}", itemRequests);
        List<Item> itemsResponse = itemRepository.findAllByItemRequestIn(itemRequests);
        log.debug("Items response: {}", itemsResponse);
        if (itemsResponse.isEmpty()) {
            log.warn("All requests have no responses");
            return itemRequests.stream().map((request) -> {
                        ItemRequestInfoDto itemRequestInfoDto =
                                itemRequestMapper.itemRequestToItemRequestInfoDto(request);
                        itemRequestInfoDto.setItems(Collections.emptyList());
                        return itemRequestInfoDto;
                    })
                    .collect(Collectors.toList());
        } else {
            Map<Long, List<Item>> responses = itemsResponse.stream()
                    .collect(Collectors.groupingBy((item) -> item.getItemRequest().getId()));
            log.debug("Item responses: {}", responses);
            return itemRequests.stream().map((request) -> {
                ItemRequestInfoDto itemRequestInfoDto = itemRequestMapper.itemRequestToItemRequestInfoDto(request);
                itemRequestInfoDto.setItems(responses.get(request.getId()).stream()
                        .map(itemMapper::itemToItemRequestInfoDtoItemDto)
                        .collect(Collectors.toList()));
                return itemRequestInfoDto;
            }).collect(Collectors.toList());
        }
    }

    @Override
    public ItemRequestInfoDto getItemRequestById(Long requestId, Long ownerId) {
        userRepository.findById(ownerId).orElseThrow(() -> {
            log.warn("User with id {} not found", ownerId);
            return new NotFoundException(String.format("User with id %d not found", ownerId));
        });
        log.debug("User with id = {} found", ownerId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
            log.warn("Item request with id {} not found", requestId);
            return new NotFoundException(
                    String.format("Item request with id %d not found", requestId));
        });
        log.debug("Item request with id = {} found", requestId);
        List<Item> responses = itemRepository.findAllByItemRequest(itemRequest);
        log.debug("Item request items: {}", responses);
        if (responses.isEmpty()) {
            log.warn("Request with id = {} have no responses", requestId);
        }
        ItemRequestInfoDto itemRequestInfoDto = itemRequestMapper.itemRequestToItemRequestInfoDto(itemRequest);
        itemRequestInfoDto.setItems(responses.stream()
                .map(itemMapper::itemToItemRequestInfoDtoItemDto).collect(Collectors.toList()));
        return itemRequestInfoDto;
    }
}
