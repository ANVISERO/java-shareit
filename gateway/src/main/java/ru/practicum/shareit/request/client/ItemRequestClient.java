package ru.practicum.shareit.request.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createItemRequest(ItemRequestDto itemRequestDto, Long requestorId) {
        return post("", requestorId, itemRequestDto);
    }

    public ResponseEntity<Object> getAllItemRequestsByRequestor(Long requestorId) {
        return get("", requestorId);
    }

    public ResponseEntity<Object> getSeveralItemRequestsPaginated(int from, int size, Long requestorId) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&size={size}", requestorId, parameters);
    }

    public ResponseEntity<Object> getItemRequestById(Long requestId, Long ownerId) {
        return get("/" + requestId, ownerId);
    }
}
