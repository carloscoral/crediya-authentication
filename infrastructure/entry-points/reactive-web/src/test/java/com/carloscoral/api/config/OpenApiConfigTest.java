package com.carloscoral.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;
    private OpenAPI openAPI;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
        openAPI = openApiConfig.customOpenAPI();
    }

    @Test
    void shouldCreateOpenAPIBean() {
        assertNotNull(openAPI, "OpenAPI bean should not be null");
    }

    @Test
    void shouldConfigureApiInfoCorrectly() {
        Info info = openAPI.getInfo();
        
        assertNotNull(info, "API info should not be null");
        assertEquals("Authentication API", info.getTitle(), "API title should match");
        assertEquals("1.0.0", info.getVersion(), "API version should match");
        assertEquals("API for user authentication and management", info.getDescription(), "API description should match");
    }

    @Test
    void shouldConfigureContactInfoCorrectly() {
        Info info = openAPI.getInfo();
        Contact contact = info.getContact();
        
        assertNotNull(contact, "Contact info should not be null");
        assertEquals("Carlos Coral", contact.getName(), "Contact name should match");
        assertEquals("coralcarlos21@gmail.com", contact.getEmail(), "Contact email should match");
    }

    @Test
    void shouldConfigureServersCorrectly() {
        List<Server> servers = openAPI.getServers();
        
        assertNotNull(servers, "Servers list should not be null");
        assertEquals(1, servers.size(), "Should have exactly one server configured");
        
        Server server = servers.get(0);
        assertEquals("http://localhost:8080", server.getUrl(), "Server URL should match");
        assertEquals("Development server", server.getDescription(), "Server description should match");
    }

    @Test
    void shouldHaveCompleteConfiguration() {
        assertNotNull(openAPI.getInfo(), "Info should be configured");
        assertNotNull(openAPI.getInfo().getContact(), "Contact should be configured");
        assertNotNull(openAPI.getServers(), "Servers should be configured");
        assertFalse(openAPI.getServers().isEmpty(), "At least one server should be configured");
    }

    @Test
    void shouldCreateNewInstanceEachTime() {
        OpenAPI firstInstance = openApiConfig.customOpenAPI();
        OpenAPI secondInstance = openApiConfig.customOpenAPI();
        
        assertNotSame(firstInstance, secondInstance, "Each call should return a new instance");
        
        assertEquals(firstInstance.getInfo().getTitle(), secondInstance.getInfo().getTitle(),
                "Both instances should have the same configuration");
    }

    @Test
    void shouldHaveValidApiInfo() {
        Info info = openAPI.getInfo();
        
        assertNotNull(info.getTitle(), "Title should not be null");
        assertNotNull(info.getVersion(), "Version should not be null");
        assertNotNull(info.getDescription(), "Description should not be null");
        
        assertFalse(info.getTitle().trim().isEmpty(), "Title should not be empty");
        assertFalse(info.getVersion().trim().isEmpty(), "Version should not be empty");
        assertFalse(info.getDescription().trim().isEmpty(), "Description should not be empty");
    }

    @Test
    void shouldHaveValidContactInfo() {
        Contact contact = openAPI.getInfo().getContact();
        
        assertNotNull(contact.getName(), "Contact name should not be null");
        assertNotNull(contact.getEmail(), "Contact email should not be null");
        
        assertFalse(contact.getName().trim().isEmpty(), "Contact name should not be empty");
        assertFalse(contact.getEmail().trim().isEmpty(), "Contact email should not be empty");
        
        assertTrue(contact.getEmail().contains("@"), "Contact email should be a valid email format");
    }

    @Test
    void shouldHaveValidServerConfiguration() {
        List<Server> servers = openAPI.getServers();
        
        servers.forEach(server -> {
            assertNotNull(server.getUrl(), "Server URL should not be null");
            assertNotNull(server.getDescription(), "Server description should not be null");
            assertFalse(server.getUrl().trim().isEmpty(), "Server URL should not be empty");
            assertFalse(server.getDescription().trim().isEmpty(), "Server description should not be empty");
        });
    }
}
