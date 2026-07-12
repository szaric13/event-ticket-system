package com.example.booking_service.config;

import com.example.booking_service.model.Booking;
import com.example.booking_service.repository.BookingRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExpirationListener implements MessageListener {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        System.out.println("EXPIRATION LISTENER TRIGGERED: " + expiredKey);
        if (expiredKey.startsWith("booking:")) {
            String bookingIdStr = expiredKey.substring("booking:".length());
            Long bookingId = Long.parseLong(bookingIdStr);
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null && booking.getStatus() == Booking.BookingStatus.PENDING) {
                booking.setStatus(Booking.BookingStatus.EXPIRED);
                bookingRepository.save(booking);
                String ticketsKey = "event:" + booking.getEventId() + ":tickets";
                redisTemplate.opsForValue().increment(ticketsKey, booking.getTicketCount());
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "booking.expired", booking.getId());
                System.out.println("Booking " + bookingId + " expired. Tickets returned.");
            }
        }
    }
}