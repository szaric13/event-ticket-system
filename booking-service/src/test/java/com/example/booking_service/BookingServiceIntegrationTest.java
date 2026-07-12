package com.example.booking_service;

import com.example.booking_service.model.Booking;
import com.example.booking_service.repository.BookingRepository;
import com.example.booking_service.service.BookingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class BookingServiceIntegrationTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Long eventId = 1L;

    @BeforeEach
    void setUp() {

        redisTemplate.opsForValue().set("event:" + eventId + ":tickets", "10");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(
                Map.of("remainingTickets", 8), HttpStatus.OK);
        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(Map.class)
        )).thenReturn(responseEntity);
    }

    @Test
    void shouldCreateBookingAndConfirm() {
        Booking booking = bookingService.createBooking(1L, eventId, 2);
        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.PENDING);

        assertThat(redisTemplate.opsForValue().get("event:" + eventId + ":tickets")).isEqualTo("10");

        String reservationKey = "booking:" + booking.getId();
        assertThat(redisTemplate.hasKey(reservationKey)).isTrue();

        Booking confirmed = bookingService.confirmBooking(booking.getId());
        assertThat(confirmed.getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
        assertThat(redisTemplate.hasKey(reservationKey)).isFalse();
    }

    @Test
    void shouldExpireBookingAndReturnTickets() {
        Booking booking = bookingService.createBooking(1L, eventId, 2);

        redisTemplate.opsForValue().decrement("event:" + eventId + ":tickets", 2);

        String reservationKey = "booking:" + booking.getId();

        byte[] channel = "__keyevent@0__:expired".getBytes();
        byte[] message = reservationKey.getBytes();
        redisTemplate.getConnectionFactory().getConnection().publish(channel, message);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Booking expired = bookingRepository.findById(booking.getId()).orElseThrow();
            assertThat(expired.getStatus()).isEqualTo(Booking.BookingStatus.EXPIRED);
        });

        assertThat(redisTemplate.opsForValue().get("event:" + eventId + ":tickets")).isEqualTo("10");
    }

    @Test
    void shouldNotConfirmExpiredBooking() {
        Booking booking = bookingService.createBooking(1L, eventId, 2);
        String reservationKey = "booking:" + booking.getId();

        redisTemplate.delete(reservationKey);

        Assertions.assertThrows(RuntimeException.class, () ->
                bookingService.confirmBooking(booking.getId())
        );
    }
}