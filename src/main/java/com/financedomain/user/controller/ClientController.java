package com.financedomain.user.controller;

import com.financedomain.user.bean.Client;
import com.financedomain.user.dto.ApiResponse;
import com.financedomain.user.dto.ClientRequest;
import com.financedomain.user.dto.PasswordUpdateRequest;
import com.financedomain.user.exception.AccountAlreadyExistException;
import com.financedomain.user.exception.BadCreationFormatException;
import com.financedomain.user.exception.NullUserDataException;
import com.financedomain.user.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/client")
public class ClientController {

    private static final String UNAUTHORIZED = "Unauthorized";
    private static final String ACCESSDENIED = "Access Denied";
    private static final String CLIENT = "CLIENT";
    private static final String ADMIN = "ADMINISTRATOR";
    private static final String INTERNAL = "INTERNAL";

    @Autowired
    private ClientService clientService;

    @Autowired
    private Environment environment;

    private String getPort() {
        return environment.getProperty("local.server.port", "unknown");
    }

    @PostMapping("/register")
    public ResponseEntity<Object> createClient(@RequestBody ClientRequest request) {
        try {
            Client client = clientService.createClient(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(client, getPort()));
        } catch (BadCreationFormatException | AccountAlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Le numméro saisi est déja utilisé : " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Object> getAllClients(
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (!ADMIN.equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        List<Client> clients = clientService.getAllClients();
        return ResponseEntity.ok(new ApiResponse<>(clients, getPort()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getClientById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (CLIENT.equals(xUserRole) && !String.valueOf(id).equals(xUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        return clientService.getClientById(id)
                .<ResponseEntity<Object>>map(client -> {
                    if (INTERNAL.equals(xUserRole)) {
                        return ResponseEntity.ok(client);
                    } else {
                        return ResponseEntity.ok(new ApiResponse<>(client, getPort()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<Object> getClientByNumber(
            @PathVariable String number,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        // Seuls l'administrateur et les appels internes (auth-service via Feign) sont autorisés
        if (!ADMIN.equals(xUserRole) && !INTERNAL.equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        return clientService.getClientByNumber(number)
                .<ResponseEntity<Object>>map(client -> {
                    if (INTERNAL.equals(xUserRole)) {
                        return ResponseEntity.ok(client);
                    } else {
                        return ResponseEntity.ok(new ApiResponse<>(client, getPort()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteClient(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (CLIENT.equals(xUserRole) && !String.valueOf(id).equals(xUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        try {
            clientService.deleteClient(id);
            return ResponseEntity.noContent().build();
        } catch (NullUserDataException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("L'id client saisi est introuvable ou n'existe pas" + e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<Object> updatePassword(
            @RequestBody PasswordUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (!CLIENT.equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        try {
            clientService.updatePassword(Long.valueOf(xUserId), request);
            return ResponseEntity.ok(new ApiResponse<>("Mot de passe modifié avec succès.", getPort()));
        } catch (NullUserDataException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadCreationFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
