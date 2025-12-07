package com.divyansh.airbnbapp.handlers;

import com.divyansh.airbnbapp.dto.OAuthResponseDTO;
import com.divyansh.airbnbapp.entity.User;
import com.divyansh.airbnbapp.security.JWTService;
import com.divyansh.airbnbapp.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JWTService jwtService;
    @Value("${frontend.url}")
    private String FRONTEND_URL;


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) token.getPrincipal();

        log.info("OAuth Success for: " + oAuth2User.getAttribute("email"));
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        log.info(name);
        OAuthResponseDTO oAuthResponseDTO = userService.loginOrCreateGoogleUser(email,name);
        User user = oAuthResponseDTO.getUser();
        if(user == null) {
            User newUser = User.builder()
                    .name(name)
                    .email(email)
                    .build();
            user = userService.save(newUser);
        }
        String[] tokens = oAuthResponseDTO.getTokens();
        log.info("Tokens from OAuth service: {}", (Object) oAuthResponseDTO.getTokens());
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens[1])
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();
        response.addHeader("Set-Cookie",cookie.toString());
        // Redirect to frontend
        response.sendRedirect(FRONTEND_URL+"/oauth/success");
    }
}
