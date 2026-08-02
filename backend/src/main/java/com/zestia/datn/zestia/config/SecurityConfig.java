package com.zestia.datn.zestia.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final EmployeeShiftAccessFilter employeeShiftAccessFilter;
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;
    private static final String ROLE_ADMIN = "ROLE_Admin";
    private static final String ROLE_NHAN_VIEN = "ROLE_Nh\u00E2n vi\u00EAn";
    private static final String ROLE_NHANVIEN = "ROLE_NhanVien";
    private static final String[] ADMIN_ROLES = { ROLE_ADMIN };
    private static final String[] STAFF_ROLES = { ROLE_ADMIN, ROLE_NHAN_VIEN, ROLE_NHANVIEN };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/google", "/api/auth/register", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/vay", "/api/vay/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/thuoc-tinh", "/api/thuoc-tinh/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/thong-bao/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/voucher").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/product/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/storefront/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/ai-chat").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/support-chat/customer/request").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/support-chat/customer/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/support-chat/customer/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/newsletter/subscribe").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/lucky-wheel/campaign").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/lucky-wheel/check", "/api/lucky-wheel/spin").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/payment/create-order", "/api/payment/apply-voucher", "/api/payment/best-voucher").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/payment/momo/create", "/api/payment/zalopay/create").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/payment/momo/return", "/api/payment/zalopay/return").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/payment/momo/ipn", "/api/payment/zalopay/callback").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/hoa-don/search").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/hoa-don/*/cancel-guest/request-otp").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/hoa-don/*/cancel-guest").permitAll()
                .requestMatchers("/api/auth/me", "/api/auth/profile/**", "/api/hoa-don/my-orders").authenticated()
                .requestMatchers("/api/customer-data/**").authenticated()
                .requestMatchers("/api/customer-notifications/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/payment/order/**", "/api/hoa-don/*/tracking").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/hoa-don/search-by-phone").hasAnyAuthority(STAFF_ROLES)
                .requestMatchers(HttpMethod.GET, "/api/hoa-don/*").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/hoa-don/*/cancel").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/returns/mine").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/returns/online").authenticated()
                .requestMatchers("/api/returns/**").hasAnyAuthority(STAFF_ROLES)
                .requestMatchers(HttpMethod.GET, "/api/khach-hang/search").hasAnyAuthority(STAFF_ROLES)
                .requestMatchers(HttpMethod.POST, "/api/khach-hang/quick").hasAnyAuthority(STAFF_ROLES)
                .requestMatchers("/api/dashboard/inventory").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers("/api/dashboard/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers("/api/admin/**", "/api/khach-hang/**", "/api/nhan-vien/**",
                        "/api/thong-bao/**", "/api/voucher", "/api/voucher/**",
                        "/api/promotions/**", "/api/lucky-wheel/admin/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.POST, "/api/lich-lam-viec/*/review-unavailable").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.POST, "/api/lich-lam-viec/*/confirm", "/api/lich-lam-viec/*/unavailable",
                        "/api/lich-lam-viec/*/check-in", "/api/lich-lam-viec/*/check-out").hasAnyAuthority(STAFF_ROLES)
                .requestMatchers(HttpMethod.POST, "/api/lich-lam-viec/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.PUT, "/api/lich-lam-viec/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.DELETE, "/api/lich-lam-viec/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.POST, "/api/vay/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.PUT, "/api/vay/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.DELETE, "/api/vay/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.POST, "/api/thuoc-tinh/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.PUT, "/api/thuoc-tinh/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.DELETE, "/api/thuoc-tinh/**").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.GET, "/api/lich-lam-viec/nhan-vien").hasAnyAuthority(ADMIN_ROLES)
                .requestMatchers(HttpMethod.GET, "/api/lich-lam-viec/**").hasAnyAuthority(STAFF_ROLES)
                .requestMatchers("/api/support-chat/staff/**").hasAnyAuthority(STAFF_ROLES)
                .requestMatchers("/api/staff/**").hasAnyAuthority(STAFF_ROLES)
                .requestMatchers("/api/pos-reservations/**").hasAnyAuthority(STAFF_ROLES)
                .requestMatchers("/api/payment/momo/qr", "/api/payment/zalopay/qr", "/api/hoa-don/**").hasAnyAuthority(STAFF_ROLES)
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(employeeShiftAccessFilter, JwtAuthFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split("\\s*,\\s*")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
