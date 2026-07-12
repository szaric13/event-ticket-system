package com.example.booking_service.service;

import com.example.booking_service.config.RabbitMQConfig;
import com.example.booking_service.model.Booking;
import com.example.booking_service.repository.BookingRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private static final int RESERVATION_TTL_SECONDS = 900;

    public Booking createBooking(Long userId, Long eventId, int ticketCount) {
        String inventoryUrl = "https://event-inventory-service.onrender.com/events/" + eventId + "/reserve";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Integer> requestMap = Map.of("count", ticketCount);
        HttpEntity<Map<String, Integer>> requestEntity = new HttpEntity<>(requestMap, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(inventoryUrl, requestEntity, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setEventId(eventId);
            booking.setTicketCount(ticketCount);
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setCreatedAt(LocalDateTime.now());
            booking = bookingRepository.save(booking);

            String reservationKey = "booking:" + booking.getId();
            String reservationData = booking.getId() + ":" + userId + ":" + eventId + ":" + ticketCount;
            redisTemplate.opsForValue().set(reservationKey, reservationData);
            redisTemplate.expire(reservationKey, Duration.ofSeconds(RESERVATION_TTL_SECONDS));
            return booking;
        } else {
            throw new RuntimeException("Nije moguće rezervisati karte: " + response.getBody());
        }
    }

    public Booking confirmBooking(Long bookingId) {
        String reservationKey = "booking:" + bookingId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(reservationKey))) {
            throw new RuntimeException("Rezervacija je istekla");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "booking.confirmed", booking.getId());

        redisTemplate.delete(reservationKey);
        return booking;
    }
}