package com.japantravel.controller;

import com.japantravel.dto.Dtos.LoginRequest;
import com.japantravel.dto.Dtos.SignupRequest;
import com.japantravel.dto.Dtos.User;
import com.japantravel.repository.UserRepository;
import com.japantravel.security.AppUserPrincipal;
import com.japantravel.security.CurrentUser;
import com.japantravel.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final UserDetailsService uds;
    private final CurrentUser currentUser;
    private final AuditService auditService;
    private final SecurityContextRepository contextRepo = new HttpSessionSecurityContextRepository();

    public AuthController(UserRepository users, PasswordEncoder encoder,
                          AuthenticationManager authManager, UserDetailsService uds,
                          CurrentUser currentUser, AuditService auditService) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.uds = uds;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req,
                                    HttpServletRequest httpReq, HttpServletResponse httpRes) {
        String username = req.username() == null ? "" : req.username().trim();
        String password = req.password() == null ? "" : req.password();
        String nickname = req.nickname() == null || req.nickname().isBlank()
                ? username : req.nickname().trim();
        if (username.length() < 3 || username.length() > 32) {
            return ResponseEntity.badRequest().body(Map.of("error", "username은 3~32자여야 합니다"));
        }
        if (password.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "password는 4자 이상이어야 합니다"));
        }
        if (users.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "이미 존재하는 username"));
        }
        long id = users.insert(username, encoder.encode(password), nickname, "USER");
        auditService.log(id, username, "SIGNUP", "user", id, null);
        login(new LoginRequest(username, password), httpReq, httpRes);
        return users.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.internalServerError().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req,
                                   HttpServletRequest httpReq, HttpServletResponse httpRes) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);
            contextRepo.saveContext(ctx, httpReq, httpRes);
            AppUserPrincipal p = (AppUserPrincipal) auth.getPrincipal();
            auditService.log(p.getId(), p.getUsername(), "LOGIN_SUCCESS", "user", p.getId(), null);
            return users.findById(p.getId()).<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.internalServerError().build());
        } catch (Exception e) {
            auditService.log(null, req.username(), "LOGIN_FAIL", null, null, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "username 또는 password가 올바르지 않습니다"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpReq) {
        AppUserPrincipal p = currentUser.principalOrNull();
        if (p != null) {
            auditService.log(p.getId(), p.getUsername(), "LOGOUT", "user", p.getId(), null);
        }
        HttpSession session = httpReq.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<User> me() {
        AppUserPrincipal p = currentUser.principalOrNull();
        if (p == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return users.findById(p.getId()).map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
