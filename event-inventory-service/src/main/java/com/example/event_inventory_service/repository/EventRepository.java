package com.example.event_inventory_service.repository;

import com.example.event_inventory_service.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}