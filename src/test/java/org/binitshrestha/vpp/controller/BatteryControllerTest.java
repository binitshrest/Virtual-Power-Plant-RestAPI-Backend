package org.binitshrestha.vpp.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.binitshrestha.vpp.model.dto.*;
import org.binitshrestha.vpp.model.entity.Battery;
import org.binitshrestha.vpp.model.entity.BatteryBuilder;
import org.binitshrestha.vpp.service.BatteryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class BatteryControllerTest {
    private MockMvc mockMvc;
    @Mock
    BatteryService batteryService;
    BatteryCreateRequest request;
    Battery battery;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BatteryController(batteryService)).build();
        request = new BatteryCreateRequestBuilder().build();
        battery = new BatteryBuilder().build();
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void testRegisterBattery() throws Exception {
        // Arrange

        BatteryResponse response = new BatteryResponseBuilder().build();

        when(batteryService.registerBattery(any(BatteryCreateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Battery"))
                .andExpect(jsonPath("$.postcode").value("12345"))
                .andExpect(jsonPath("$.wattCapacity").value(500.0));

        verify(batteryService, times(1)).registerBattery(any(BatteryCreateRequest.class));
    }
    @Test
    void testRegisterBatteriesBatch() throws Exception {
        // Arrange
        BatteryCreateRequest request1 = new BatteryCreateRequestBuilder().withName("Duracell").build();
        BatteryCreateRequest request2 = new BatteryCreateRequestBuilder().withName("EverReady").build();

        BatteryBatchCreateRequest batchRequest = new BatteryBatchCreateRequestBuilder()
                .withBatteries(List.of(request1, request2)) // Ensure the list is not null
                .build();

        List<BatteryResponse> responses = List.of(new BatteryResponseBuilder().build());

        when(batteryService.registerBatteriesBatch(batchRequest)).thenReturn(responses);

        // Act & Assert
        mockMvc.perform(post("/api/v1/batteries/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(batchRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Battery"))
                .andExpect(jsonPath("$[0].postcode").value("12345"))
                .andExpect(jsonPath("$[0].wattCapacity").value(500.0));

        verify(batteryService, times(1)).registerBatteriesBatch(any(BatteryBatchCreateRequest.class));
    }

    @Test
    void testGetAllBatteries() throws Exception {
        // Arrange
        List<BatteryResponse> batteries = List.of(
                new BatteryResponseBuilder().withId(1L).withName("Battery A").withPostcode("12345").build(),
                new BatteryResponseBuilder().withId(2L).withName("Battery B").withPostcode("67890").build()
        );

        when(batteryService.getAllBatteries()).thenReturn(batteries);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Battery A"))
                .andExpect(jsonPath("$[0].postcode").value("12345"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Battery B"))
                .andExpect(jsonPath("$[1].postcode").value("67890"));


        verify(batteryService, times(1)).getAllBatteries();
    }

    @Test
    void testGetBatteryById() throws Exception {
        // Arrange
        Long batteryId = 1L;
        BatteryResponse response = new BatteryResponseBuilder()
                .withId(batteryId)
                .withName("Test Battery")
                .withPostcode("12345")
                .build();

        when(batteryService.getBatteryById(batteryId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/{id}", batteryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(batteryId))
                .andExpect(jsonPath("$.name").value("Test Battery"))
                .andExpect(jsonPath("$.postcode").value("12345"));

        verify(batteryService, times(1)).getBatteryById(batteryId);
    }

    @Test
    void testGetBatteriesInPostcodeRange() throws Exception {
        // Arrange
        String startPostcode = "1000";
        String endPostcode = "2000";
        Double minWattCapacity = 100.0;
        Double maxWattCapacity = 500.0;

        PostcodeRangeResponse response = new PostcodeRangeResponse(); // Populate with mock data as needed

        when(batteryService.getBatteriesInPostcodeRange(any(PostcodeRangeRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/postcode-range")
                        .param("startPostcode", startPostcode)
                        .param("endPostcode", endPostcode)
                        .param("minWattCapacity", String.valueOf(minWattCapacity))
                        .param("maxWattCapacity", String.valueOf(maxWattCapacity))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(batteryService, times(1)).getBatteriesInPostcodeRange(any(PostcodeRangeRequest.class));
    }

    @Test
    void testUpdateBatteryStatus() throws Exception {
        // Arrange
        Long batteryId = 1L;
        Battery.BatteryStatus newStatus = Battery.BatteryStatus.ACTIVE;

        BatteryResponse response = new BatteryResponseBuilder()
                .withId(batteryId)
                .withName("Test Battery")
                .withPostcode("12345")
                .build();

        when(batteryService.updateBatteryStatus(batteryId, newStatus)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/batteries/{id}/status", batteryId)
                        .param("status", newStatus.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(batteryId))
                .andExpect(jsonPath("$.name").value("Test Battery"))
                .andExpect(jsonPath("$.postcode").value("12345"));

        verify(batteryService, times(1)).updateBatteryStatus(batteryId, newStatus);
    }

    @Test
    void testGetHighCapacityBatteries() throws Exception {
        // Arrange
        Double minCapacity = 100.0;
        List<BatteryResponse> batteries = List.of(
                new BatteryResponseBuilder().withId(1L).withName("Battery A").withPostcode("12345").build(),
                new BatteryResponseBuilder().withId(2L).withName("Battery B").withPostcode("67890").build()
        );

        when(batteryService.getHighCapacityBatteries(minCapacity)).thenReturn(batteries);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/high-capacity")
                        .param("minCapacity", String.valueOf(minCapacity))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Battery A"))
                .andExpect(jsonPath("$[0].postcode").value("12345"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Battery B"))
                .andExpect(jsonPath("$[1].postcode").value("67890"));

        verify(batteryService, times(1)).getHighCapacityBatteries(minCapacity);
    }

    @Test
    void testGetStatisticsByPostcode() throws Exception {
        // Arrange
        List<PostcodeStatistics> statistics = List.of(
                new PostcodeStatistics("12345", 2, 1000.0 , 2000),
                new PostcodeStatistics("67890", 3, 1500.0, 1500)
        );

        when(batteryService.getStatisticsByPostcode()).thenReturn(statistics);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/statistics/by-postcode")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postcode").value("12345"))
                .andExpect(jsonPath("$[0].batteryCount").value(2))
                .andExpect(jsonPath("$[0].totalCapacity").value(1000.0))
                .andExpect(jsonPath("$[1].postcode").value("67890"))
                .andExpect(jsonPath("$[1].batteryCount").value(3))
                .andExpect(jsonPath("$[1].totalCapacity").value(1500.0));

        verify(batteryService, times(1)).getStatisticsByPostcode();
    }

    @Test
    void testGetTotalBatteryCount() throws Exception {
        // Arrange
        List<BatteryResponse> batteries = List.of(
                new BatteryResponseBuilder().withId(1L).withName("Battery A").withPostcode("12345").build(),
                new BatteryResponseBuilder().withId(2L).withName("Battery B").withPostcode("67890").build()
        );

        when(batteryService.getAllBatteries()).thenReturn(batteries);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(batteries.size()));

        verify(batteryService, times(1)).getAllBatteries();
    }

    @Test
    void testSearchBatteriesByName() throws Exception {
        // Arrange
        String namePattern = "Battery";
        List<BatteryResponse> batteries = List.of(
                new BatteryResponseBuilder().withId(1L).withName("Battery A").withPostcode("12345").build(),
                new BatteryResponseBuilder().withId(2L).withName("Battery B").withPostcode("67890").build()
        );

        when(batteryService.getAllBatteries()).thenReturn(batteries);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/search")
                        .param("namePattern", namePattern)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Battery A"))
                .andExpect(jsonPath("$[0].postcode").value("12345"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Battery B"))
                .andExpect(jsonPath("$[1].postcode").value("67890"));

        verify(batteryService, times(1)).getAllBatteries();
    }

    @Test
    void testGetUniquePostcodes() throws Exception {
        // Arrange
        List<BatteryResponse> batteries = List.of(
                new BatteryResponseBuilder().withId(1L).withName("Battery A").withPostcode("12345").build(),
                new BatteryResponseBuilder().withId(2L).withName("Battery B").withPostcode("67890").build(),
                new BatteryResponseBuilder().withId(3L).withName("Battery C").withPostcode("12345").build()
        );

        when(batteryService.getAllBatteries()).thenReturn(batteries);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/postcodes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("12345"))
                .andExpect(jsonPath("$[1]").value("67890"));

        verify(batteryService, times(1)).getAllBatteries();
    }

    @Test
    void testHealthCheck() throws Exception {
        List<BatteryResponse> batteries = List.of(
                new BatteryResponseBuilder().withId(1L).withName("Battery A").withPostcode("12345").build(),
                new BatteryResponseBuilder().withId(2L).withName("Battery B").withPostcode("67890").build(),
                new BatteryResponseBuilder().withId(3L).withName("Battery C").withPostcode("12345").build()
        );
        // Arrange
        int totalBatteries = 3; // Example value;

        when(batteryService.getAllBatteries()).thenReturn(batteries);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("Battery service is operational"))
                .andExpect(jsonPath("$.totalBatteries").value(totalBatteries));

        verify(batteryService, times(1)).getAllBatteries();
    }

    @Test
    void testHealthCheck_ServiceUnavailable() throws Exception {
        // Arrange
        String errorMessage = "Database connection failed";
        when(batteryService.getAllBatteries()).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/api/v1/batteries/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.message").value("Battery service is experiencing issues: " + errorMessage))
                .andExpect(jsonPath("$.totalBatteries").value(0));

        verify(batteryService, times(1)).getAllBatteries();
    }

}
