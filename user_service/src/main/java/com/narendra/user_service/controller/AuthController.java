package com.narendra.user_service.controller;

import com.narendra.user_service.dtos.requests.LoginRequest;
import com.narendra.user_service.dtos.requests.SignupRequest;
import com.narendra.user_service.dtos.responses.AccessResponse;
import com.narendra.user_service.dtos.responses.ApiResponse;
import com.narendra.user_service.jwt.JwtService;
import com.narendra.user_service.model.User;
import com.narendra.user_service.repository.UserRepository;
import com.narendra.user_service.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.narendra.user_service.utils.Utils.clearJwtCookie;
import static com.narendra.user_service.utils.Utils.setJwtCookie;

@RestController
@RequestMapping("/api/auth")
public class AuthController{

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SignupRequest request, HttpServletResponse response) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // Encode password and create new user
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(newUser);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(newUser);
        String refreshToken = jwtService.generateRefreshToken(newUser);

        // Set tokens in cookies
        setJwtCookie(response, "accessToken", accessToken, 600);
        setJwtCookie(response, "refreshToken", refreshToken, 604800);

       // return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        return ResponseEntity.ok(new ApiResponse<>(true , "User Registered Successfully" ,  new AccessResponse(accessToken , refreshToken)));

    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            // Authenticate user credentials
           authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Load user details after successful authentication
            User user = (User) userDetailsService.loadUserByUsername(request.getEmail());

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Set tokens as HttpOnly cookies
            setJwtCookie(response, "accessToken", accessToken, 600);      // 10 minutes
            setJwtCookie(response, "refreshToken", refreshToken, 604800); // 7 days

            // Return success message or user info if needed
            return ResponseEntity.ok(new ApiResponse<>(true, "User login Successfully", new AccessResponse(accessToken, refreshToken)));

        } catch (BadCredentialsException ex) {
            // Invalid email or password
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid email or password", null));
        } catch (Exception ex) {
            // Catch all other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred during login", null));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing refresh token");
        }

        try {
            String username = jwtService.extractUserEmail(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtService.validateToken(refreshToken, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
            }

            if (!(userDetails instanceof User user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("UserDetails is not an instance of User");
            }

             String newAccessToken = jwtService.generateAccessToken(user);
             String newRefreshToken = jwtService.generateRefreshToken(user);

            return ResponseEntity.ok(new AccessResponse(newAccessToken, newRefreshToken));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        clearJwtCookie(response, "accessToken");
        clearJwtCookie(response, "refreshToken");
        return ResponseEntity.ok(new ApiResponse<>(true , "Logout Successfully", ""));
    }
}
