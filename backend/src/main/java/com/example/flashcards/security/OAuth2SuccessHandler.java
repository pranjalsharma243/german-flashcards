package com.example.flashcards.security;

import com.example.flashcards.model.AuthResponse;
import com.example.flashcards.service.OAuth2GoogleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2GoogleService oauth2GoogleService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public OAuth2SuccessHandler(OAuth2GoogleService oauth2GoogleService) {
        this.oauth2GoogleService = oauth2GoogleService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String googleSub = oauth2User.getAttribute("sub");

        if (email == null || googleSub == null) {
            response.sendRedirect(frontendUrl + "?error=missing_email");
            return;
        }

        AuthResponse authResponse = oauth2GoogleService.findOrCreateUser(email, googleSub);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", URLEncoder.encode(authResponse.token(), StandardCharsets.UTF_8))
                .queryParam("username", URLEncoder.encode(authResponse.username(), StandardCharsets.UTF_8))
                .queryParam("role", URLEncoder.encode(authResponse.role(), StandardCharsets.UTF_8))
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
