package com.example.flashcards.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Component
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_MAX_AGE = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return cookieValue(request).flatMap(this::deserialize).orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, serialize(authorizationRequest));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest saved = loadAuthorizationRequest(request);
        Cookie clear = new Cookie(COOKIE_NAME, "");
        clear.setPath("/");
        clear.setHttpOnly(true);
        clear.setMaxAge(0);
        response.addCookie(clear);
        return saved;
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(authorizationRequest);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    private Optional<OAuth2AuthorizationRequest> deserialize(String value) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(value);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                return Optional.of((OAuth2AuthorizationRequest) ois.readObject());
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<String> cookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> !v.isBlank())
                .findFirst();
    }
}
