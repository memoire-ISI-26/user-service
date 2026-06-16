package com.financedomain.user.controller;

import com.financedomain.user.bean.Client;
import com.financedomain.user.dto.ClientRequest;
import com.financedomain.user.exception.BadCreationFormatException;
import com.financedomain.user.exception.NullUserDataException;
import com.financedomain.user.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/client")
public class ClientController {

    private final static String Unauthorized = "Unauthorized";
    private final static String AccessDenied = "Access Denied";
    private final static String CLIENT ="CLIENT";
    private final static String ADMIN ="ADMIN";
    private final static String INTERNAL = "INTERNAL";

    @Autowired
    private ClientService clientService;

    @PostMapping("/register")
    public ResponseEntity<?> createClient(@RequestBody ClientRequest request) {
        try {
            Client client = clientService.createClient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(client);
        } catch (BadCreationFormatException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Le numméro saisi est déja utilisé" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllClients(
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Unauthorized);
        }
        if (!ADMIN.equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AccessDenied);
        }
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Unauthorized);
        }
        if (CLIENT.equals(xUserRole) && !String.valueOf(id).equals(xUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AccessDenied);
        }
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<?> getClientByNumber(
            @PathVariable String number,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Unauthorized);
        }
        // INTERNAL role is used by authentication-service Feign calls during login
        if (!INTERNAL.equals(xUserRole) && CLIENT.equals(xUserRole) && !number.equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AccessDenied);
        }
        return clientService.getClientByNumber(number)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Unauthorized);
        }
        if (CLIENT.equals(xUserRole) && !String.valueOf(id).equals(xUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AccessDenied);
        }
        try {
            clientService.deleteClient(id);
            return ResponseEntity.noContent().build();
        } catch (NullUserDataException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("L'id client saisi est introuvable ou n'existe pas" + e.getMessage());
        }
    }
}
