package com.example.notification_service.messaging;


import com.example.notification_service.model.Notification;
import com.example.notification_service.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationListener {

    @Autowired
    private NotificationRepository notificationRepository;

    @RabbitListener(queues = "booking.confirmed")
    public void handleConfirmed(Long bookingId) {
        Notification notif = new Notification();
        notif.setBookingId(bookingId);
        notif.setType("CONFIRMED");
        notif.setMessage("Booking " + bookingId + " confirmed. E-tickets sent to email.");
        notif.setTimestamp(LocalDateTime.now());
        notificationRepository.save(notif);
        System.out.println("✅ Notification sent: CONFIRMED for booking " + bookingId);
    }

    @RabbitListener(queues = "booking.expired")
    public void handleExpired(Long bookingId) {
        Notification notif = new Notification();
        notif.setBookingId(bookingId);
        notif.setType("EXPIRED");
        notif.setMessage("Booking " + bookingId + " expired. Tickets returned to inventory.");
        notif.setTimestamp(LocalDateTime.now());
        notificationRepository.save(notif);
        System.out.println("⚠️ Notification sent: EXPIRED for booking " + bookingId);
    }
}