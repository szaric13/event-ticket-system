package com.example.event_inventory_service.config;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class TicketsReservationScript {

    private static final String LUA_SCRIPT =
            "local key = KEYS[1]\n" +
                    "local requested = tonumber(ARGV[1])\n" +
                    "local available = redis.call('GET', key)\n" +
                    "if available == false then\n" +
                    "   return -1  -- event ne postoji u Redis-u\n" +
                    "end\n" +
                    "local availableNum = tonumber(available)\n" +
                    "if availableNum >= requested then\n" +
                    "   redis.call('DECRBY', key, requested)\n" +
                    "   return availableNum - requested  -- vraćamo preostali broj\n" +
                    "else\n" +
                    "   return -2  -- nema dovoljno karata\n" +
                    "end";

    public DefaultRedisScript<Long> getReserveScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LUA_SCRIPT);
        script.setResultType(Long.class);
        return script;
    }
}