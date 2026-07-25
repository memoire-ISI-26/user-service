package com.financedomain.user.proxy.fallback;

import com.financedomain.user.dto.AccountCreationRequest;
import com.financedomain.user.proxy.WalletProxy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletProxyFallback implements WalletProxy {

    @Override
    public ResponseEntity<?> createAccount(AccountCreationRequest request) {
        System.err.println("[Fallback] wallet-service est indisponible. Impossible d'ouvrir un portefeuille pour le numéro : " + request.getNumber());

        throw new RuntimeException("Le service financier (wallet-service) est actuellement indisponible. Veuillez réessayer plus tard.");
    }
}
