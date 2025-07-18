package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Map;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }


    @Bean
    SecurityFilterChain httpSecurityFilterChain(HttpSecurity security) throws Exception {

        return security
                .with(authorizationServer(), as -> as.oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .webAuthn(config -> config
                        .rpId("localhost")
                        .rpName("bootiful")
                        .allowedOrigins("http://localhost:9090", "http://127.0.0.1:9090")
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .oneTimeTokenLogin(ott -> {
                    ott.tokenGenerationSuccessHandler((request, response, oneTimeToken) -> {


                        var msg = "please go to http://localhost:9090/login/ott?token=" +
                                oneTimeToken.getTokenValue();
                        System.out.println(msg);

                        response.getWriter().write("you've got console mail!");
                        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                    });
                })
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsPasswordService userDetailsPasswordService(JdbcUserDetailsManager detailsManager) {
        return (user, newPassword) -> {
            var build = User.withUserDetails(user).password(newPassword).build();
            detailsManager.updateUser(build);
            return build;
        };
    }

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }
//
//    @Bean
//    UserDetailsService userDetailsService(PasswordEncoder pw) {
//        var pw1 = pw.encode("pw");
//        var pw2 = pw.encode("pw");
//        System.out.println("pw1: " + pw1);
//        System.out.println("pw2: " + pw2);
//        var users = Set.of(
//                User.withUsername("jlong").password(pw1).roles("USER").build(),
//                User.withUsername("rwinch").password(pw2).roles("USER").build()
//        );
//        return new InMemoryUserDetailsManager(users);
//    }
//
}

@Controller
@ResponseBody
class HelloController {

    @GetMapping("/")
    Map<String, String> hello(Principal principal) {
        return Map.of("name", principal.getName());
    }
}