package com.example.oauth2server.global.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

    /**
     * Protocol endpoints 를 위한 설정
     */
    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.
                getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(withDefaults());	// OpenID Connect 1.0 사용
        http.
                exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor( // 인가 실패에 대한 처리를 정의
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
        http.oauth2ResourceServer((resourceServer) -> resourceServer.jwt(withDefaults())); // '토큰 검증'에 대한 설정

        return http.build();
    }

    /**
     * 인증(Authentication)을 위한 설정
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authorize -> {
            authorize
                    .requestMatchers("/").permitAll()
                    .anyRequest().authenticated();
        })
                .formLogin(withDefaults());

        return http.build();
    }
//저는 B/NB는 일반적으로 IO 작업중 쓰레드 블록킹을 하는지를 기준으로 생각하고요. S/AS는 작업 결과를 받아오는 방식으로 이해합니다.
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails userDetails = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("1234")
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(userDetails);
//    }

    /**
     * 클라이언트의 정보를 등록하고 관리하는 역할을 한다.
     */
//    @Bean
//    public RegisteredClientRepository registeredClientRepository() {
//        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
//                .clientName("Your client name")
//                .clientId("your-client")
//                .clientSecret("{noop}your-secret") // 실제 운영환경에서는 임의의 문자열을 사용하고, 코드에 올리면 안됨
//                .clientAuthenticationMethods(methods -> {
//                    methods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
//                })
//                .authorizationGrantTypes(types -> {
//                    types.add(AuthorizationGrantType.AUTHORIZATION_CODE);
//                    types.add(AuthorizationGrantType.REFRESH_TOKEN);
//                })
//                .redirectUris(uri -> {
//                    uri.add("http://localhost:3000");
//                })
//                .postLogoutRedirectUri("http://localhost:3000")
//                .scopes(scope -> {
//                    scope.add(OidcScopes.OPENID);
//                    scope.add(OidcScopes.PROFILE);
//                })
//                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
//                .build();
//
//        return new InMemoryRegisteredClientRepository(oidcClient);
//    }

    /**
     * jwt 생성에 필요한 RSA키 generate, 실제 운영에 사용하려면 KeyStore에 저장해야한다.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return keyPair;
    }

    /**
     * 토큰 검증을 위한 디코더
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Authorization server를 구성하기 위한 여러 EndPoint를 설정하는 객체
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .build();

        // AuthorizationServerSettings Class를 통해서 endpoints 설정 가능
//        return AuthorizationServerSettings.builder()
//                .issuer("https://example.com")
//                .authorizationEndpoint("/oauth2/v1/authorize")
//                .deviceAuthorizationEndpoint("/oauth2/v1/device_authorization")
//                .deviceVerificationEndpoint("/oauth2/v1/device_verification")
//                .tokenEndpoint("/oauth2/v1/token")
//                .tokenIntrospectionEndpoint("/oauth2/v1/introspect")
//                .tokenRevocationEndpoint("/oauth2/v1/revoke")
//                .jwkSetEndpoint("/oauth2/v1/jwks")
//                .oidcLogoutEndpoint("/connect/v1/logout")
//                .oidcUserInfoEndpoint("/connect/v1/userinfo")
//                .oidcClientRegistrationEndpoint("/connect/v1/register")
//                .build();
    }
}
