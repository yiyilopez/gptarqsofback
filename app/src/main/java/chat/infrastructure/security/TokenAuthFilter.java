package chat.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {
    // Demo: tokens válidos y su cuota (puedes cargar de config)
    private static final Map<String, Integer> validTokens = new ConcurrentHashMap<>();
    static {
        validTokens.put("demo-token", 30); // 30 req/min
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Let preflight CORS requests proceed so Spring's CORS handling adds headers
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Falta token");
            return;
        }
        String token = auth.substring(7);
        if (!validTokens.containsKey(token)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token inválido");
            return;
        }
        // Rate limit simple: puedes mejorar esto
        filterChain.doFilter(request, response);
    }
}
