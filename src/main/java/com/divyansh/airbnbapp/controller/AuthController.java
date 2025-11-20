package com.divyansh.airbnbapp.controller;

import com.divyansh.airbnbapp.advice.ApiError;
import com.divyansh.airbnbapp.advice.ApiResponse;
import com.divyansh.airbnbapp.dto.*;
import com.divyansh.airbnbapp.entity.User;
import com.divyansh.airbnbapp.security.AuthService;
import com.divyansh.airbnbapp.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody SignUpRequestDTO signUpRequestDTO) {
        return new ResponseEntity<>(authService.signUp(signUpRequestDTO), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        String[] tokens = authService.login(loginDTO);


        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens[1])
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .domain("localhost")
                .maxAge(7 * 24 * 60 * 60)
        .build();
        httpServletResponse.setHeader("Set-Cookie",cookie.toString());

        return ResponseEntity.ok(new LoginResponseDTO(tokens[0]));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(HttpServletRequest httpServletRequest) {
        System.out.println("COOKIES → " + Arrays.toString(httpServletRequest.getCookies()));
        log.info("Cookie: {}",Arrays.toString(httpServletRequest.getCookies()));
        String refreshToken = Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh Token not found inside the cookies"));

        String accessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDTO(accessToken));
    }

    @PostMapping("/google/login")
    public ResponseEntity<?> googleLogin(
            HttpServletResponse response,
            @RequestBody Map<String, String> body
    ) throws Exception {

        String idTokenString = body.get("credential");
        // 1. Verify Google ID token
        GoogleIdToken idTokenObj = googleIdTokenVerifier.verify(idTokenString);
        if (idTokenObj == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid Google token"));
        }

        GoogleIdToken.Payload payload = idTokenObj.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // 2. Your existing logic
        OAuthResponseDTO oAuthResponseDTO = userService.loginOrCreateGoogleUser(email, name);
        String accessToken = oAuthResponseDTO.getTokens()[0];
        String refreshToken = oAuthResponseDTO.getTokens()[1];

        // 3. Set REFRESH TOKEN as HttpOnly secure cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)       // true in production
                .sameSite("None")
                .path("/")
                .domain("localhost")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 4. Return ACCESS TOKEN in JSON
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "message", "Google login success"
        ));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest req,HttpServletResponse res){
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)       // true in production
                .sameSite("None")
                .path("/")
                .domain("localhost")
                .maxAge(0)
                .build();

        res.addHeader("Set-Cookie", deleteCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
