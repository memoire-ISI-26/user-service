package com.financedomain.user.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.financedomain.user.dto.AccountCreationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service")
public interface WalletProxy {

    @PostMapping("/accounts")
    ResponseEntity<?> createAccount(@RequestBody AccountCreationRequest request);
}
