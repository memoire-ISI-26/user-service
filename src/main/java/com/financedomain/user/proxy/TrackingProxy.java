package com.financedomain.user.proxy;

import com.financedomain.user.dto.TrackingEvent;
import com.financedomain.user.proxy.fallback.TrackingProxyFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tracking-service", fallback = TrackingProxyFallback.class)
public interface TrackingProxy {

    @PostMapping("/tracking/event")
    ResponseEntity<Void> collectEvent(
            @RequestBody TrackingEvent event,
            @RequestHeader("X-User-Role") String xUserRole
    );
}
