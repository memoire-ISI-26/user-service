package com.financedomain.user.controller;

import com.financedomain.user.bean.Admin;
import com.financedomain.user.dto.AdminRequest;
import com.financedomain.user.exception.BadCreationFormatException;
import com.financedomain.user.exception.NullUserDataException;
import com.financedomain.user.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/admin")
public class AdminController {

    private static final String UNAUTHORIZED = "Unauthorized";
    private static final String ACCESSDENIED = "Access Denied";
    private static final String ADMIN ="ADMIN";
    private static final String INTERNAL = "INTERNAL";

    @Autowired
    private AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<?> createAdmin(@RequestBody AdminRequest request) {
        try {
            Admin admin = adminService.createAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(admin);
        } catch (BadCreationFormatException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllAdmins(
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (!ADMIN.equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (!ADMIN.equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        return adminService.getAdminById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getAdminByUsername(
            @PathVariable String username,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        // INTERNAL role is used by authentication-service Feign calls during login
        if (!ADMIN.equals(xUserRole) && !INTERNAL.equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        return adminService.getAdminByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (!ADMIN.equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        try {
            adminService.deleteAdmin(id);
            return ResponseEntity.noContent().build();
        } catch (NullUserDataException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
