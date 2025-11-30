package br.ifsp.demo.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import pitest.fasterxml.jackson.core.JsonProcessingException;
import pitest.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;
public class IntegrationTestUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String VALID_USER_ID = "test@user.com";
    public static final String INVALID_USER_ID = "";

    public static String asJsonString(final Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Falha ao converter objeto para JSON", e);
        }
    }

    public static HttpHeaders createHeaders(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User", userId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public static HttpHeaders createValidHeaders() {
        return createHeaders(VALID_USER_ID);
    }

    @TestConfiguration
    public static class TestSecurityConfig {

        @Bean
        public AuthenticationManager authenticationManager() {
            return mock(AuthenticationManager.class);
        }

        @Bean
        public br.ifsp.demo.security.config.JwtService jwtService() {
            return mock(br.ifsp.demo.security.config.JwtService.class);
        }
    }
}
