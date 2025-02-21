package com.example.todolist.config;

import com.example.todolist.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/*file ini buatt ngatur konfigurasi seperti menentukan perizinan, aturan cors, authentifikasi, dll*/
@Configuration
public class SecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;
    private final UserService userService;
    public SecurityConfig(JwtRequestFilter jwtRequestFilter, UserService userService) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.userService=userService;
    }

    //method untuk konfigurasi keamanan spring
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //http.csrf disable csrf (cross-site request forgery)
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(corsCustomizer -> corsCustomizer
                    .configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration corsConfiguration = new CorsConfiguration();
                        corsConfiguration.setAllowCredentials(true); //mengizinkan kredensial
                        corsConfiguration.addAllowedHeader("*");// mengizinkan akses ke semua header
                        corsConfiguration.addAllowedMethod("*"); // mengizinkan akses ke semua method (GET, POST, PUT, DELETE)
                        corsConfiguration.addAllowedOriginPattern("*"); //mengizinkan akses ke semua origin
                        corsConfiguration.setMaxAge(3600L); //mengatur waktu kedaluwarsa
                        return corsConfiguration;
                    }
                }))
                //pengaturan otorisasi (akses endpoint)
                .authorizeHttpRequests(session -> session
                        .requestMatchers(HttpMethod.GET,"/api/user/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/user/get/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/user/search/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/user/register").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/user/login").permitAll()
                        .requestMatchers(HttpMethod.PUT,"/api/user/update/*").permitAll()
                        .requestMatchers(HttpMethod.PUT,"/api/user/update/admin/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/user/delete/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,"/api/todolist/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/todolist/**").permitAll()
                        .requestMatchers(HttpMethod.PUT,"/api/todolist/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE,"/api/todolist/**").permitAll()

                        .requestMatchers(HttpMethod.GET,"/api/category/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/category/**").permitAll()
                        .requestMatchers(HttpMethod.PUT,"/api/category/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE,"/api/category/**").permitAll()
                        .anyRequest().authenticated() 
                )
                //pengaturan session untuk tidak menyimpan user didalam session tetapi memakai jwt
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //method buat authentifikasi user
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    //ngambil data user dari database
    @Bean
    public UserDetailsService userDetailService(){
        return userService::loadUserByUsername;
    }
    //method buat encode password
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    //method buat authentifikasi
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
