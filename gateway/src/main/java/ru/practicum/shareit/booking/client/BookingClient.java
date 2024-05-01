package ru.practicum.shareit.booking.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createBooking(BookingDto bookingDto, Long bookerId) {
        return post("", bookerId, bookingDto);
    }

    public ResponseEntity<Object> changeBookingStatus(Long bookingId, Boolean approved, Long ownerId) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", ownerId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(Long bookingId, Long userId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllUserBookingsByStatus(BookingStatus bookingStatus, Long userId,
                                                             int from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size,
                "state", bookingStatus.name()
        );
        return get("?from={from}&size={size}&state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getAllOwnerItemsBookingsByStatus(BookingStatus bookingStatus, Long ownerId,
                                                                   int from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size,
                "state", bookingStatus.name()
        );
        return get("/owner?from={from}&size={size}&state={state}", ownerId, parameters);
    }
}
