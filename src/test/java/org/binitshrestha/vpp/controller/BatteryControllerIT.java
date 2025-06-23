package org.binitshrestha.vpp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.binitshrestha.vpp.model.dto.BatteryCreateRequest;
import org.binitshrestha.vpp.model.dto.BatteryCreateRequestBuilder;
import org.binitshrestha.vpp.model.dto.BatteryResponse;
import org.binitshrestha.vpp.model.dto.BatteryResponseBuilder;
import org.binitshrestha.vpp.repository.BatteryRepository;
import org.binitshrestha.vpp.service.BatteryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest
@Import({BatteryController.class, BatteryService.class})
@AutoConfigureMockMvc
@Testcontainers
class BatteryControllerIT {
    private MockMvc mockMvc;

    @Autowired
    private BatteryService batteryService;

    @Autowired
    private BatteryRepository batteryRepository;

    private BatteryCreateRequest request;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                    .withUsername("postgres")
                    .withPassword("password")
                    .withExposedPorts(5432);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url",postgres::getJdbcUrl);
        registry.add("spring.datasource.username",postgres::getUsername);
        registry.add("spring.datasource.password",postgres::getPassword);
    }

    @BeforeAll
    static void beforeAll(){
        postgres.start();
    }

    @AfterAll
    static void afterAll(){
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new BatteryController(batteryService))
                .build();
        request = new BatteryCreateRequestBuilder().build();

    }

    @AfterEach
    void tearDown(){
        batteryRepository.deleteAll();
    }

    @Test
    void testRegisterBattery() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Duracell"))
                .andExpect(jsonPath("$.postcode").value("44600"))
                .andExpect(jsonPath("$.wattCapacity").value(150.0));

    }
}