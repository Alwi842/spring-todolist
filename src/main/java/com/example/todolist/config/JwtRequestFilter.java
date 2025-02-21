package com.example.todolist.config;

import com.example.todolist.service.UserService;
import com.example.todolist.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//untuk authentifikasi dan validasi token

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    private final UserService userService;

    @Autowired
    public JwtRequestFilter(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        //ambil informasi token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); //ambil token jwt urutan ke 7
            username = jwtUtil.extractUsername(jwt); //ambil username dari token diekstrak
        }
        //validasi user
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //nampung objek yang isinya informasi username, pass, role/otoritas
            UserDetails userDetails = this.userService.loadUserByUsername(username);
            //nampung objek yang isinya adalah user yang sudah di validasi
            if(jwtUtil.validateToken(jwt, userDetails)){
                //nampung objek yang isinya informasi unam,pass,role
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                //buat nambahin detail informasi dari request yang dikirim
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //menetapkan user telah terauthentifikasi dan teroterisasi
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        //buat jalanin konfigurasi filter
        filterChain.doFilter(request, response);
    }
}
