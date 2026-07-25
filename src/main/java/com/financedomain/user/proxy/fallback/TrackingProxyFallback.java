package com.financedomain.user.proxy.fallback;

import com.financedomain.user.dto.TrackingEvent;
import com.financedomain.user.proxy.TrackingProxy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TrackingProxyFallback implements TrackingProxy {

    @Override
    public ResponseEntity<?> collectEvent(TrackingEvent event, String xUserRole) {
        System.err.println("[Fallback] tracking-service est indisponible. Événement de tracking ignoré : " + event.getEventType());

        return ResponseEntity.ok().build();
    }
}
