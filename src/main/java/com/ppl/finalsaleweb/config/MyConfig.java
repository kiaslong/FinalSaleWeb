package com.ppl.finalsaleweb.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class MyConfig {



    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager manager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }




    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(r ->
                        r.requestMatchers(HttpMethod.GET, "/users/list","/users/current-user","/users/profile-image/{filename:.+}"
                                       , "/users/link"
                                        ,"/products","/products/product_images/{filename:.+}"
                                        ,"/orders","/orders/analytics"
                                        ,"/customers","/customers/{id}" ,"/customers/{id}/orderhistory"

                                        ).permitAll()
                                .requestMatchers(HttpMethod.POST, "/users/register",
                                        "/users/login","/users/verify","/users/resend-email/{userId}"
                                           ,"/products/add"
                                        ,"/orders"
                                ).permitAll()

                                .requestMatchers(HttpMethod.PUT,"/users/{id}","/users/toggle-lock/{userId}","/users/change-password/{id}"
                                            ,"/products/{barcode}"
                                ).permitAll()
                                .requestMatchers(HttpMethod.DELETE,"/users/{id}","/products/{barcode}").permitAll()
                                .requestMatchers(HttpMethod.PATCH,"/users/update-profile/{id}").permitAll()
                                .anyRequest().authenticated()

                )

                .build();
    }
}