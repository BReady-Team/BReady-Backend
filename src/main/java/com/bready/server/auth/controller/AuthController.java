package com.bready.server.auth.controller;

import com.bready.server.auth.dto.SignupRequest;
import com.bready.server.auth.dto.SignupResponse;
import com.bready.server.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }
}
