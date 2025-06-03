package com.narendra.user_service.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class Utils {

    public static void setJwtCookie(HttpServletResponse response, String name, String token, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Set to false if not using HTTPS (for development)
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setDomain("localhost"); // Set your domain (optional)
        response.addCookie(cookie);
    }

    public static void clearJwtCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setDomain("localhost"); // Match domain
        response.addCookie(cookie);
    }
}
