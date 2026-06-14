package com.example.demo.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.enums.Perfil;
import com.example.demo.exceptions.InvalidTokenException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extrairToken(request);

        if (rotaPublica(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                jwtService.validarToken(token);
            } catch (InvalidTokenException exception) {
                escreverErroToken(response, exception);
                return;
            }

            Integer usuarioId = jwtService.getUsuarioId(token);
            Perfil perfil = jwtService.getPerfil(token);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    usuarioId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name())));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void escreverErroToken(HttpServletResponse response, InvalidTokenException exception) throws IOException {
        response.setStatus(exception.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"code":"%s","message":"%s","timestamp":"%s"}
                """.formatted(exception.getErrorCode(), exception.getMessage(), LocalDateTime.now()));
    }

    private String extrairToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7);
    }

    private boolean rotaPublica(HttpServletRequest request) {
        String path = request.getServletPath();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.equals("/auth/login")
                || path.equals("/auth/idoso/login")
                || path.equals("/auth/verificar-2fa")
                || path.equals("/auth/reenviar-codigo")
                || path.equals("/auth/recuperar-senha")
                || path.equals("/auth/verificar-recuperacao")
                || path.equals("/auth/nova-senha")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/");
    }
}
