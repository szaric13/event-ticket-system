package com.example.booking_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;     // u pravom sistemu bi bio entitet User
    private Long eventId;
    private int ticketCount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;  // PENDING, CONFIRMED, EXPIRED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters/setters, konstruktori...
    // Enum:
    public enum BookingStatus {
        PENDING, CONFIRMED, EXPIRED
    }
}