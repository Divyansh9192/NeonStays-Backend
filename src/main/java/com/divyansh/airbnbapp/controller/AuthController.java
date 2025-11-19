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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody SignUpRequestDTO signUpRequestDTO){
        return new ResponseEntity<>(authService.signUp(signUpRequestDTO), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){

        String[] tokens = authService.login(loginDTO);

        Cookie cookie = new Cookie("refreshToken",tokens[1]);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(7*24*60*60);
        httpServletResponse.addCookie(cookie);

        return ResponseEntity.ok(new LoginResponseDTO(tokens[0]));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(HttpServletRequest httpServletRequest){
        String refreshToken = Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(()->new AuthenticationServiceException("Refresh Token not found inside the cookies"));

        String accessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDTO(accessToken));
    }
//    @PostMapping("/google")
//    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body,HttpServletResponse httpServletResponse) throws Exception {
//        String idTokenString = body.get("idToken");
//
//        GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
//        if (idToken == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new ApiResponse<>("Invalid ID token"));
//        }
//
//        GoogleIdToken.Payload payload = idToken.getPayload();
//
//        String email = payload.getEmail();
//        String name = (String) payload.get("name");
//
//        OAuthResponseDTO oAuthResponseDTO = userService.loginOrCreateGoogleUser(email, name);
//        Cookie cookie = new Cookie("refreshToken",oAuthResponseDTO.getTokens()[1]);
//        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
//        cookie.setMaxAge(7*24*60*60);
//        httpServletResponse.addCookie(cookie);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        Map.of(
//                                "accessToken", oAuthResponseDTO.getTokens()[0],
//                                "user", oAuthResponseDTO.getUser().getName()
//                        )
//                )
//        );
//    }
@PostMapping("/google/callback")
public void googleCallback(
        HttpServletResponse response,
        HttpServletRequest request,
        @RequestParam(value = "credential", required = false) String credential,
        @RequestParam(value = "id_token", required = false) String idToken
) throws Exception {
    System.out.println("PATH = " + request.getServletPath());

    String idTokenString = credential != null ? credential : idToken;

    if (idTokenString == null) {
        response.sendRedirect("http://localhost:5173/login?error=missing_token");
        return;
    }

    GoogleIdToken idTokenObj = googleIdTokenVerifier.verify(idTokenString);
    if (idTokenObj == null) {
        response.sendRedirect("http://localhost:5173/login?error=invalid_token");
        return;
    }

    GoogleIdToken.Payload payload = idTokenObj.getPayload();
    String email = payload.getEmail();
    String name = (String) payload.get("name");

    OAuthResponseDTO oAuthResponseDTO = userService.loginOrCreateGoogleUser(email, name);

    String accessToken = oAuthResponseDTO.getTokens()[0];
    String refreshToken = oAuthResponseDTO.getTokens()[1];

    Cookie cookie = new Cookie("refreshToken", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(7 * 24 * 60 * 60);
    response.addCookie(cookie);

    response.sendRedirect("http://localhost:5173/login?access=" + accessToken);
}



}
