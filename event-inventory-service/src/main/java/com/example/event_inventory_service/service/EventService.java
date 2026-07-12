package com.example.event_inventory_service.service;

import com.example.event_inventory_service.config.TicketsReservationScript;
import com.example.event_inventory_service.model.Event;
import com.example.event_inventory_service.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TicketsReservationScript ticketsReservationScript;  // ← OVO JE FALILO

    // Inicijalizacija Redis ključa za karte pri kreiranju događaja
    public Event createEvent(Event event) {
        Event saved = eventRepository.save(event);
        // Ključ: event:<id>:tickets, vrednost: kapacitet
        redisTemplate.opsForValue().set("event:" + saved.getId() + ":tickets", String.valueOf(saved.getCapacity()));
        return saved;
    }

    // Vraća listu događaja sa preostalim brojem karata iz Redisa
    public List<Map<String, Object>> getAllEventsWithAvailability() {
        List<Event> events = eventRepository.findAll();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Event event : events) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", event.getId());
            map.put("name", event.getName());
            map.put("description", event.getDescription());
            map.put("eventDate", event.getEventDate());
            map.put("capacity", event.getCapacity());
            String ticketsStr = redisTemplate.opsForValue().get("event:" + event.getId() + ":tickets");
            int availableTickets = ticketsStr != null ? Integer.parseInt(ticketsStr) : event.getCapacity();
            map.put("availableTickets", availableTickets);
            result.add(map);
        }
        return result;
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public long reserveTickets(Long eventId, int count) {
        DefaultRedisScript<Long> script = ticketsReservationScript.getReserveScript();  // ← Koristi injektovanu instancu
        Long result = redisTemplate.execute(
                script,
                List.of("event:" + eventId + ":tickets"),
                String.valueOf(count)
        );
        if (result == null) {
            throw new RuntimeException("Redis execution error");
        }
        return result; // -1, -2, ili preostali broj
    }
}