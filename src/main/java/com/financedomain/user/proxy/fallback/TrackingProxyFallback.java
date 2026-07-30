package com.financedomain.user.proxy.fallback;

import com.financedomain.user.dto.TrackingEvent;
import com.financedomain.user.proxy.TrackingProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrackingProxyFallback implements TrackingProxy {

    @Override
    public ResponseEntity<?> collectEvent(TrackingEvent event, String xUserRole) {
        log.warn("[Fallback] tracking-service est indisponible. Événement de tracking ignoré : {}", event.getEventType());
        return ResponseEntity.ok().build();
    }
}
