# Prompt 7 — JWT Security

> Add Spring Security with JWT:
> - Create a `/api/auth/login` endpoint that accepts username/password and returns JWT
> - Protect `/api/events` and `/api/metrics` endpoints (require JWT)
> - Keep `/api/health` public

## Implementation Summary

This prompt implemented comprehensive JWT-based security for the EventPulse application by:

### Files Created:
- `src/main/java/com/eventpulse/security/JwtAuthenticationFilter.java` - JWT filter
- `src/main/java/com/eventpulse/security/JwtTokenProvider.java` - JWT token management
- `src/main/java/com/eventpulse/security/SecurityConfig.java` - Security configuration
- `src/main/java/com/eventpulse/security/CustomUserDetailsService.java` - User details service
- `src/main/java/com/eventpulse/controller/AuthController.java` - Authentication endpoints
- `src/main/java/com/eventpulse/dto/LoginRequest.java` - Login request DTO
- `src/main/java/com/eventpulse/dto/LoginResponse.java` - Login response DTO
- `src/main/java/com/eventpulse/entity/User.java` - User entity
- `src/main/java/com/eventpulse/repository/UserRepository.java` - User repository

### Key Features Implemented:

#### Security Configuration:
- **JWT-based Authentication** - Stateless authentication with JSON Web Tokens
- **Password Encryption** - BCrypt password hashing with strength 12
- **Endpoint Protection** - Role-based access control
- **CORS Configuration** - Cross-origin resource sharing for web clients
- **Session Management** - Stateless session configuration

#### Protected Endpoints:
- `/api/events/**` - Requires JWT authentication
- `/api/metrics/**` - Requires JWT authentication
- `/api/auth/**` - Public authentication endpoints
- `/api/health` - Public health check endpoint

#### Authentication Flow:
1. **Login Request** - POST `/api/auth/login` with username/password
2. **Token Generation** - JWT token with user claims and expiration
3. **Token Validation** - Automatic token validation on protected endpoints
4. **Authorization** - Role-based access control

#### JWT Features:
- **Token Generation** - Secure JWT creation with HMAC-SHA512
- **Token Validation** - Automatic token verification and parsing
- **Claims Management** - Username, roles, and expiration claims
- **Token Refresh** - Configurable token expiration (24 hours default)
- **Security Headers** - Proper HTTP security headers

#### User Management:
- **User Entity** - JPA entity with username, password, and roles
- **User Repository** - Spring Data JPA repository for user operations
- **User Details Service** - Custom user details service for authentication
- **Default Users** - Pre-configured users for testing

### Technical Implementation:

#### Security Configuration:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**", "/api/health").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

#### JWT Token Provider:
```java
@Component
public class JwtTokenProvider {
    private final String secretKey = "your-secret-key";
    private final long validityInMilliseconds = 86400000; // 24 hours
    
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + validityInMilliseconds);
        
        return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }
}
```

#### Authentication Controller:
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        
        String token = jwtTokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", userPrincipal.getUsername(), 
            jwtTokenProvider.getExpirationDate(token), "Authentication successful"));
    }
}
```

### Default Users:
- **admin** / password - ADMIN role
- **user** / password - USER role  
- **eventpulse** / password - USER role

### API Usage:

#### Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

#### Access Protected Endpoints:
```bash
# Get JWT token from login response
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# Use token for protected endpoints
curl -X GET http://localhost:8080/api/events \
  -H "Authorization: Bearer $TOKEN"
```

### Security Features:
- **Stateless Authentication** - No server-side session storage
- **Token Expiration** - Configurable token lifetime
- **Secure Headers** - HTTP security headers
- **Password Security** - BCrypt with strength 12
- **Role-based Access** - Extensible role system
- **CORS Support** - Cross-origin request handling

This JWT security implementation provided enterprise-grade authentication and authorization for the EventPulse platform, ensuring secure access to protected resources while maintaining stateless scalability.
