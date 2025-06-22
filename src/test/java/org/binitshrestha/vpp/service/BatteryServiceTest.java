package org.binitshrestha.vpp.service;

import org.binitshrestha.vpp.exception.BatteryAlreadyExistsException;
import org.binitshrestha.vpp.exception.BatteryNotFoundException;
import org.binitshrestha.vpp.model.dto.*;
import org.binitshrestha.vpp.model.entity.Battery;
import org.binitshrestha.vpp.model.entity.BatteryBuilder;
import org.binitshrestha.vpp.repository.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class BatteryServiceTest {

    @InjectMocks
    BatteryService batteryService;
    @Mock
    BatteryRepository batteryRepository;
    BatteryCreateRequest request;
    Battery battery;

    @BeforeEach
    public void setup() {
        request = new BatteryCreateRequestBuilder().build();
        battery = new BatteryBuilder().build();
    }

    @Test
    void registerBattery() {
        //Arrange
        when(batteryRepository.existsByName(request.getName())).thenReturn(false);
        when(batteryRepository.save(any(Battery.class))).thenReturn(battery);

        //Act
        BatteryResponse response = batteryService.registerBattery(request);


        // Assert
        assertNotNull(response);
        assertEquals(battery.getId(), response.getId());
        assertEquals(battery.getName(), response.getName());
        assertEquals(battery.getPostcode(), response.getPostcode());
        assertEquals(battery.getWattCapacity(), response.getWattCapacity());
        verify(batteryRepository).existsByName(request.getName());
        verify(batteryRepository).save(any(Battery.class));
    }

    @Test
    void registerBattery_WhenBatteryAlreadyExists() {
        // Arrange
        when(batteryRepository.existsByName(request.getName())).thenReturn(true);

        // Act & Assert
        BatteryAlreadyExistsException exception = assertThrows(
                BatteryAlreadyExistsException.class,
                () -> batteryService.registerBattery(request)
        );

        assertEquals("Battery with name '" + request.getName() + "' already exists", exception.getMessage());
        verify(batteryRepository).existsByName(request.getName());
        verify(batteryRepository, never()).save(any(Battery.class));
    }

    @Test
    void registerBatteriesBatch() {
        BatteryCreateRequest request1 = new BatteryCreateRequestBuilder().build();
        BatteryCreateRequest request2 = new BatteryCreateRequestBuilder().withName("EverReady").build();
        BatteryBatchCreateRequest batchRequest = new BatteryBatchCreateRequest(List.of(request1, request2));

        Battery battery1 = new BatteryBuilder().build();
        Battery battery2 = new BatteryBuilder().withName("EverReady").build();

        when(batteryRepository.existsByName(request1.getName())).thenReturn(false);
        when(batteryRepository.existsByName(request2.getName())).thenReturn(false);
        when(batteryRepository.save(any(Battery.class)))
                .thenReturn(battery1)
                .thenReturn(battery2);

        List<BatteryResponse> responses = batteryService.registerBatteriesBatch(batchRequest);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());

        verify(batteryRepository, times(2)).existsByName(anyString());
        verify(batteryRepository, times(2)).save(any(Battery.class));
    }

    @Test
    void getBatteriesInPostcodeRange() {
    }

    @Test
    void getAllBatteries_ShouldReturnSortedBatteryResponses() {
        // Arrange
        Battery battery1 = new BatteryBuilder().withName("EverReady").build();
        Battery battery2 = new BatteryBuilder().withName("Duracell").build();
        Battery battery3 = new BatteryBuilder().withName("Panasonic").build();

        when(batteryRepository.findAll()).thenReturn(List.of(battery1, battery2, battery3));

        // Act
        List<BatteryResponse> responses = batteryService.getAllBatteries();

        // Assert
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals("Duracell", responses.get(0).getName());
        assertEquals("EverReady", responses.get(1).getName());
        assertEquals("Panasonic", responses.get(2).getName());

        verify(batteryRepository).findAll();
    }

    @Test
    void getBatteryById_ShouldReturnBatteryResponse_WhenBatteryExists() {
        // Arrange
        Long batteryId = 1L;
        Battery battery = new BatteryBuilder().withId(batteryId).build();
        when(batteryRepository.findById(batteryId)).thenReturn(Optional.of(battery));

        // Act
        BatteryResponse response = batteryService.getBatteryById(batteryId);

        // Assert
        assertNotNull(response);
        assertEquals(battery.getId(), response.getId());
        assertEquals(battery.getName(), response.getName());
        assertEquals(battery.getPostcode(), response.getPostcode());
        assertEquals(battery.getWattCapacity(), response.getWattCapacity());
        verify(batteryRepository).findById(batteryId);
    }

    @Test
    void getBatteryById_ShouldThrowException_WhenBatteryDoesNotExist() {
        // Arrange
        Long batteryId = 1L;
        when(batteryRepository.findById(batteryId)).thenReturn(Optional.empty());

        // Act & Assert
        BatteryNotFoundException exception = assertThrows(
                BatteryNotFoundException.class,
                () -> batteryService.getBatteryById(batteryId)
        );

        assertEquals("Battery with ID " + batteryId + " not found", exception.getMessage());
        verify(batteryRepository).findById(batteryId);
    }

    @Test
    void updateBatteryStatus_ShouldUpdateStatus_WhenBatteryExists() {
        // Arrange
        Long batteryId = 1L;
        Battery.BatteryStatus newStatus = Battery.BatteryStatus.ACTIVE;
        Battery battery = new BatteryBuilder().withId(batteryId).build();
        when(batteryRepository.findById(batteryId)).thenReturn(Optional.of(battery));
        when(batteryRepository.save(any(Battery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BatteryResponse response = batteryService.updateBatteryStatus(batteryId, newStatus);

        // Assert
        assertNotNull(response);
        assertEquals(batteryId, response.getId());
        assertEquals(newStatus.name(), response.getStatus());
        verify(batteryRepository).findById(batteryId);
        verify(batteryRepository).save(battery);
    }

    @Test
    void updateBatteryStatus_ShouldThrowException_WhenBatteryDoesNotExist() {
        // Arrange
        Long batteryId = 1L;
        Battery.BatteryStatus newStatus = Battery.BatteryStatus.ACTIVE;
        when(batteryRepository.findById(batteryId)).thenReturn(Optional.empty());

        // Act & Assert
        BatteryNotFoundException exception = assertThrows(
                BatteryNotFoundException.class,
                () -> batteryService.updateBatteryStatus(batteryId, newStatus)
        );

        assertEquals("Battery with ID " + batteryId + " not found", exception.getMessage());
        verify(batteryRepository).findById(batteryId);
        verify(batteryRepository, never()).save(any(Battery.class));
    }

    @Test
    void getHighCapacityBatteries_ShouldReturnFilteredAndSortedBatteries() {
        // Arrange
        Double minCapacity = 100.0;

        Battery battery1 = new BatteryBuilder().withName("Battery1").withId(1L).withWattCapacity(200.0).build();
        Battery battery2 = new BatteryBuilder().withName("Battery2").withId(2L).withWattCapacity(150.0).build();
        Battery battery3 = new BatteryBuilder().withName("Battery3").withId(3L).withWattCapacity(50.0).build();

        when(batteryRepository.findAll()).thenReturn(List.of(battery1, battery2, battery3));

        // Act
        List<BatteryResponse> responses = batteryService.getHighCapacityBatteries(minCapacity);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Battery1", responses.get(0).getName());
        assertEquals("Battery2", responses.get(1).getName());
        verify(batteryRepository).findAll();
    }

    @Test
    void testGetBatteriesInPostcodeRange() {
        // Arrange
        PostcodeRangeRequest request = new PostcodeRangeRequest("1000", "2000", null, null);
        Battery battery1 = new BatteryBuilder().withName("Battery1").withId(1L).withWattCapacity(200.0).build();
        Battery battery2 = new BatteryBuilder().withName("Battery2").withId(2L).withWattCapacity(150.0).build();
        Battery battery3 = new BatteryBuilder().withName("Battery3").withId(3L).withWattCapacity(50.0).build();
        List<Battery> batteries = List.of(
                battery1, battery2, battery3
        );
        Object[] statistics = new Object[]{
                new Object[]{2L, 1200.0, 600.0, 500.0, 700.0}
        };

        when(batteryRepository.findBatteriesInPostcodeRange("1000", "2000")).thenReturn(batteries);
        when(batteryRepository.getBatteryStatisticsInPostcodeRange("1000", "2000")).thenReturn(statistics);

        // Act
        PostcodeRangeResponse response = batteryService.getBatteriesInPostcodeRange(request);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.getBatteryNames().size());
        assertEquals("Battery1", response.getBatteryNames().get(0));
        assertEquals("Battery2", response.getBatteryNames().get(1));

        PostcodeRangeResponse.BatteryStatistics stats = response.getStatistics();
        assertNotNull(stats);
        assertEquals(2, stats.getTotalBatteries());
        assertEquals(700.0, stats.getMaxWattCapacity());

        verify(batteryRepository, times(1)).findBatteriesInPostcodeRange("1000", "2000");
        verify(batteryRepository, times(1)).getBatteryStatisticsInPostcodeRange("1000", "2000");
    }
    @Test
    void testGetStatisticsByPostcode() {
        // Arrange
        Battery battery1 = new BatteryBuilder().withPostcode("12345").withWattCapacity(500.0).build();
        Battery battery2 = new BatteryBuilder().withPostcode("12345").withWattCapacity(700.0).build();
        Battery battery3 = new BatteryBuilder().withPostcode("67890").withWattCapacity(300.0).build();
        Battery battery4 = new BatteryBuilder().withPostcode("67890").withWattCapacity(400.0).build();

        when(batteryRepository.findAll()).thenReturn(List.of(battery1, battery2, battery3, battery4));

        // Act
        List<PostcodeStatistics> statistics = batteryService.getStatisticsByPostcode();

        // Assert
        assertNotNull(statistics);
        assertEquals(2, statistics.size());

        PostcodeStatistics stats1 = statistics.get(0);
        assertEquals("12345", stats1.getPostcode());
        assertEquals(2, stats1.getBatteryCount());
        assertEquals(1200.0, stats1.getTotalCapacity());
        assertEquals(600.0, stats1.getAverageCapacity());

        PostcodeStatistics stats2 = statistics.get(1);
        assertEquals("67890", stats2.getPostcode());
        assertEquals(2, stats2.getBatteryCount());
        assertEquals(700.0, stats2.getTotalCapacity());
        assertEquals(350.0, stats2.getAverageCapacity());

        verify(batteryRepository, times(1)).findAll();
    }

}