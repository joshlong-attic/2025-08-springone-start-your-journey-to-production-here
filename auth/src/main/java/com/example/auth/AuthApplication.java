package com.example.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;

import java.io.IOException;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    SecurityFilterChain myAuthServerSpringSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(Customizer.withDefaults())
                .webAuthn( wa -> wa
                        .allowedOrigins("http://localhost:8081", "http://127.0.0.1:8081", "http://localhost:9090")
                        .rpId("localhost")
                        .rpName("Bootiful Auth")
                ) //passkeys
//                .oneTimeTokenLogin( ot -> {
//                    ot.tokenGenerationSuccessHandler(new OneTimeTokenGenerationSuccessHandler() {
//                        @Override
//                        public void handle(HttpServletRequest request, HttpServletResponse response,
//                                           OneTimeToken oneTimeToken) throws IOException, ServletException {
////                            /String tokenValue =":9090/ott/token?" oneTimeToken.getTokenValue();
//                        }
//                    });
//                })
                .authorizeHttpRequests(ae -> ae.anyRequest().authenticated())
                .with(authorizationServer(), as -> as.oidc(Customizer.withDefaults()))
                .build();
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        // {sha}sflefoslds
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder pwdEncoder) {
        var josh = User.withUsername("jlong")
                .password(pwdEncoder.encode("pw"))
                .roles("USER")
                .build();
        var rob = User.withUsername("rwinch")
                .password(pwdEncoder.encode("pw"))
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(rob, josh);
    }

}
