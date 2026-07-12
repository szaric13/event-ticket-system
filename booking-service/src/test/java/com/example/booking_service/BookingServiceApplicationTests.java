package com.example.booking_service;

import com.example.booking_service.model.Booking;
import com.example.booking_service.repository.BookingRepository;
import com.example.booking_service.service.BookingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceApplicationTests {

	@MockitoBean
	private RestTemplate restTemplate;

	@MockitoBean
	private StringRedisTemplate stringRedisTemplate;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private BookingRepository bookingRepository;

	@Test
	void contextLoads() {
		assertThat(bookingService).isNotNull();
	}

	@Test
	void testCreateBooking() {

		ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
		when(valueOps.get(anyString())).thenReturn("10");

		when(restTemplate.postForEntity(
				anyString(),
				any(HttpEntity.class),
				eq(Map.class)
		)).thenReturn(new ResponseEntity<>(Map.of("remainingTickets", 8), HttpStatus.OK));

		Booking booking = bookingService.createBooking(1L, 1L, 2);
		assertThat(booking).isNotNull();
		assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.PENDING);
	}

	@Test
	void testConfirmBooking() {

		Booking booking = new Booking();
		booking.setUserId(1L);
		booking.setEventId(1L);
		booking.setTicketCount(2);
		booking.setStatus(Booking.BookingStatus.PENDING);
		booking = bookingRepository.save(booking);

		when(stringRedisTemplate.hasKey("booking:" + booking.getId())).thenReturn(true);

		Booking confirmed = bookingService.confirmBooking(booking.getId());
		assertThat(confirmed.getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
	}
}