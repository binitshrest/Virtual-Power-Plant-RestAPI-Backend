package org.binitshrestha.vpp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.binitshrestha.vpp.model.dto.BatteryBatchCreateRequest;
import org.binitshrestha.vpp.model.dto.BatteryBatchCreateRequestBuilder;
import org.binitshrestha.vpp.model.dto.BatteryCreateRequest;
import org.binitshrestha.vpp.model.dto.BatteryCreateRequestBuilder;
import org.binitshrestha.vpp.model.entity.Battery;
import org.binitshrestha.vpp.model.entity.BatteryITBuilder;
import org.binitshrestha.vpp.repository.BatteryRepository;
import org.binitshrestha.vpp.service.BatteryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Import({BatteryController.class, BatteryService.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BatteryControllerIT extends BaseIntegrationTest{
    private MockMvc mockMvc;

    @Autowired
    private BatteryService batteryService;

    @Autowired
    private BatteryRepository batteryRepository;

    private BatteryCreateRequest request;
    private List<Battery> saveBatteries;

    @Transactional
    @BeforeEach
    void setUp() {
        batteryRepository.deleteAll(); // Clear the database to avoid conflicts
        batteryRepository.flush();
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new BatteryController(batteryService))
                .build();
        request = new BatteryCreateRequestBuilder().build();
        saveBatteries = batteryRepository.saveAllAndFlush(List.of(
                new BatteryITBuilder().withName("Battery A").build(),
                new BatteryITBuilder().withName("Battery B").build()
        ));
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
                .andExpect(jsonPath("$.name").value("Duracell"))
                .andExpect(jsonPath("$.postcode").value("44600"))
                .andExpect(jsonPath("$.wattCapacity").value(150.0));
    }
    @Test
    void testRegisterBatteriesBatch() throws Exception {
        // Arrange
        BatteryCreateRequest request1 = new BatteryCreateRequestBuilder().withName("Duracell").build();
        BatteryCreateRequest request2 = new BatteryCreateRequestBuilder().withName("EverReady").build();

        BatteryBatchCreateRequest batchRequest = new BatteryBatchCreateRequestBuilder()
                .withBatteries(List.of(request1, request2)) // Ensure the list is not null
                .build();
        // Act & Assert
        mockMvc.perform(post("/api/v1/batteries/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(batchRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("Duracell"))
                .andExpect(jsonPath("$[0].postcode").value("44600"))
                .andExpect(jsonPath("$[0].wattCapacity").value(150.0));
    }

    @Test
    void testGetAllBatteries() throws Exception {

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Battery A"))
                .andExpect(jsonPath("$[0].postcode").value("44600"))
                .andExpect(jsonPath("$[1].name").value("Battery B"))
                .andExpect(jsonPath("$[1].postcode").value("44600"));
    }
}