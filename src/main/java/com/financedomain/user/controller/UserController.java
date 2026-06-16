package com.financedomain.user.controller;

import com.financedomain.user.bean.User;
import com.financedomain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final static String Unauthorized = "Unauthorized";
    private final static String AccessDenied = "Access Denied";
    private final static String CLIENT ="CLIENT";
    private final static String ADMIN ="ADMIN";

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Unauthorized);
        }
        if (!ADMIN.equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AccessDenied);
        }
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Unauthorized);
        }
        if (CLIENT.equals(xUserRole) && !String.valueOf(id).equals(xUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AccessDenied);
        }
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok((Object) user))
                .orElse(ResponseEntity.notFound().build());
    }
}
