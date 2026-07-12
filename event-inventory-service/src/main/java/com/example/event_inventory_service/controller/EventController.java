package com.example.event_inventory_service.controller;


import com.example.event_inventory_service.model.Event;
import com.example.event_inventory_service.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    // Kreiranje događaja (admin)
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event created = eventService.createEvent(event);
        return ResponseEntity.ok(created);
    }

    // Lista događaja sa brojem preostalih karata
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEventsWithAvailability());
    }

    // Pojedinačni događaj (za internu upotrebu)
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }


    @PostMapping("/{id}/reserve")
    public ResponseEntity<?> reserveTickets(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int count = body.get("count");
        long result = eventService.reserveTickets(id, count);
        if (result == -1) {
            return ResponseEntity.badRequest().body("Event not found in Redis");
        } else if (result == -2) {
            return ResponseEntity.badRequest().body("Not enough tickets");
        } else {
            return ResponseEntity.ok(Map.of("remainingTickets", result));
        }
    }
}