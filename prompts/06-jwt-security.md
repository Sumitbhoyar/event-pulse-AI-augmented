# Phase 6: JWT Security Implementation

## Objective
Implement JWT-based authentication with Spring Security to protect API endpoints.

## Requirements

- Create `/api/auth/login` endpoint accepting username/password
- Return JWT token on successful login
- Protect `/api/events` and `/api/metrics` endpoints (require JWT)
- Keep `/api/health` public
- Stateless session management

## Implementation Steps

### 1. Added Security Dependencies

Updated `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<!-- Plus jjwt-impl and jjwt-jackson -->
```

### 2. JWT Utilities

Created `JwtUtils.java`:
```java
public class JwtUtils {
    private final Key key;
    private final long validityMs;
    
    public String generateToken(String username) {
        // Generate JWT with expiration
    }
    
    public String extractUsername(String token) {
        // Extract username from token
    }
}
```

**Features**:
- HS256 algorithm
- Configurable secret (base64-encoded)
- Configurable expiration (default 1 hour)
- Claims extraction

### 3. JWT Authentication Filter

Created `JwtAuthenticationFilter.java`:
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
```

**Logic**:
1. Extract `Authorization` header
2. Check for `Bearer <token>` format
3. Validate and parse token
4. Set authentication in SecurityContext
5. Continue filter chain

### 4. Security Configuration

Created `SecurityConfig.java`:

**Features**:
- CSRF disabled (stateless API)
- Stateless session management
- Endpoint-based security rules
- JWT filter registration

**Public endpoints**:
- `/api/health`
- `/api/auth/login`
- `/h2-console/**`
- `/ws`, `/ws/**`
- `/swagger-ui/**`
- `/actuator/**`

**Protected endpoints**:
- `/api/events` (POST, GET)
- `/api/metrics` (GET)

### 5. Authentication Controller

Created `AuthController.java`:

```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    String token = jwtUtils.generateToken(request.getUsername());
    return ResponseEntity.ok(new LoginResponse(token));
}
```

**Note**: Demo implementation accepts any username/password.

### 6. DTOs

Created:
- `LoginRequest.java` - username, password (validated)
- `LoginResponse.java` - token

## Files Created

- `src/main/java/com/eventpulse/config/SecurityConfig.java`
- `src/main/java/com/eventpulse/security/JwtUtils.java`
- `src/main/java/com/eventpulse/security/JwtAuthenticationFilter.java`
- `src/main/java/com/eventpulse/controller/AuthController.java`
- `src/main/java/com/eventpulse/dto/LoginRequest.java`
- `src/main/java/com/eventpulse/dto/LoginResponse.java`

## Technical Details

### JWT Token Structure

**Header**:
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload**:
```json
{
  "sub": "username",
  "iat": 1697640000,
  "exp": 1697643600
}
```

**Signature**: HMAC-SHA256 with secret key

### Security Filter Chain

```
Request
  ↓
JwtAuthenticationFilter (extracts & validates token)
  ↓
Spring Security Filter Chain
  ↓
Controller
```

### Token Validation Flow

```
1. Extract Authorization header
2. Remove "Bearer " prefix
3. Parse JWT token
4. Validate signature
5. Check expiration
6. Extract username
7. Create Authentication object
8. Set in SecurityContext
```

## Challenges Encountered

### JWT Secret Key Size
**Problem**: Initial secret was 224 bits (too small).

**Error**: `The specified key byte array is 224 bits which is not secure enough`

**Solution**: Generated 256-bit (32-byte) base64-encoded secret:
```
RXZlbnRQdWxzZVNlY3VyZUtleUZvckpXVEF1dGhlbnRpY2F0aW9uMjU2Qml0cw==
```

### Basic Auth Popup
**Problem**: Swagger UI showed HTTP Basic Auth popup.

**Cause**: `.httpBasic(Customizer.withDefaults())` in SecurityConfig

**Solution**: Removed httpBasic configuration, kept only JWT.

### Filter Ordering
**Issue**: JWT filter must run before UsernamePasswordAuthenticationFilter.

**Solution**:
```java
http.addFilterBefore(new JwtAuthenticationFilter(jwtUtils()), 
    UsernamePasswordAuthenticationFilter.class);
```

## Security Considerations

### Production Recommendations

1. **Secret Management**:
   - Use environment variables
   - Rotate secrets regularly
   - Never commit secrets to git

2. **Token Expiration**:
   - Short-lived tokens (1 hour)
   - Implement refresh tokens
   - Revocation mechanism

3. **HTTPS**:
   - Always use TLS in production
   - Redirect HTTP to HTTPS

4. **Rate Limiting**:
   - Implement on /api/auth/login
   - Prevent brute force attacks

## Outcome

✅ JWT authentication fully implemented  
✅ Login endpoint working  
✅ Token generation and validation  
✅ Endpoints properly secured  
✅ Public endpoints accessible  
✅ Stateless session management  
✅ 256-bit secure secret key

