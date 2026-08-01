package com.financedomain.user.proxy.fallback;

import com.financedomain.user.dto.AccountCreationRequest;
import com.financedomain.user.exception.NotAvailableException;
import com.financedomain.user.proxy.WalletProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletProxyFallback implements WalletProxy {

    @Override
    public ResponseEntity<Object> createAccount(AccountCreationRequest request) {
        log.warn("[Fallback] wallet-service est indisponible. Impossible d'ouvrir un portefeuille pour le numéro : {}", request.getNumber());
        throw new NotAvailableException("Le service financier (wallet-service) est actuellement indisponible. Veuillez réessayer plus tard.");
    }
}
