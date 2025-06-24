package org.binitshrestha.vpp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.binitshrestha.vpp.exception.BatteryNotFoundException;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void testGetBatteryById() throws Exception {
        Long batteryId = batteryRepository.findByName("Battery A").map(Battery::getId).orElseThrow(() -> new BatteryNotFoundException("Battery Not Found"));
        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/{id}", batteryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Battery A"))
                .andExpect(jsonPath("$.postcode").value("44600"));
    }

    @Test
    void testGetBatteriesInPostcodeRange() throws Exception {
        // Arrange
        String startPostcode = "40000";
        String endPostcode = "50000";
        Double minWattCapacity = 100.0;
        Double maxWattCapacity = 500.0;
        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/postcode-range")
                        .param("startPostcode", startPostcode)
                        .param("endPostcode", endPostcode)
                        .param("minWattCapacity", String.valueOf(minWattCapacity))
                        .param("maxWattCapacity", String.valueOf(maxWattCapacity))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateBatteryStatus() throws Exception {
        // Arrange
        Long batteryId = batteryRepository.findByName("Battery A").map(Battery::getId).orElseThrow(() -> new BatteryNotFoundException("Battery Not Found"));
        Battery.BatteryStatus newStatus = Battery.BatteryStatus.ACTIVE;

        // Act & Assert
        mockMvc.perform(put("/api/v1/batteries/{id}/status", batteryId)
                        .param("status", newStatus.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Battery A"))
                .andExpect(jsonPath("$.postcode").value("44600"));
    }

    @Test
    void testGetHighCapacityBatteries() throws Exception {
        // Arrange
        Double minCapacity = 100.0;
        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/high-capacity")
                        .param("minCapacity", String.valueOf(minCapacity))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Battery A"))
                .andExpect(jsonPath("$[0].postcode").value("44600"))
                .andExpect(jsonPath("$[1].name").value("Battery B"))
                .andExpect(jsonPath("$[1].postcode").value("44600"));
    }
    @Test
    void testGetStatisticsByPostcode() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/statistics/by-postcode")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postcode").value("44600"))
                .andExpect(jsonPath("$[0].batteryCount").value(2))
                .andExpect(jsonPath("$[0].totalCapacity").value(300.0));

    }

    @Test
    void testGetTotalBatteryCount() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(saveBatteries.size()));
    }

    @Test
    void testSearchBatteriesByName() throws Exception {
        // Arrange
        String namePattern = "Battery";
        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/search")
                        .param("namePattern", namePattern)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Battery A"))
                .andExpect(jsonPath("$[0].postcode").value("44600"))
                .andExpect(jsonPath("$[1].name").value("Battery B"))
                .andExpect(jsonPath("$[1].postcode").value("44600"));
    }

    @Test
    void testGetUniquePostcodes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/postcodes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("44600"));
    }

    @Test
    void testHealthCheck() throws Exception {
        // Arrange
        int totalBatteries = 2; // Example value;
        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("Battery service is operational"))
                .andExpect(jsonPath("$.totalBatteries").value(totalBatteries));
    }
}