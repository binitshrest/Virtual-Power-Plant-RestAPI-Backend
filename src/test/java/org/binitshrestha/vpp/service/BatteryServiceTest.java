package org.binitshrestha.vpp.service;

import org.binitshrestha.vpp.exception.BatteryAlreadyExistsException;
import org.binitshrestha.vpp.exception.BatteryNotFoundException;
import org.binitshrestha.vpp.model.*;
import org.binitshrestha.vpp.model.dto.BatteryCreateRequestBuilder;
import org.binitshrestha.vpp.repository.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
    void getStatisticsByPostcode_ShouldReturnPostcodeStatistics() {
        // Arrange
        Battery battery1 = new BatteryBuilder().withPostcode("44600").withWattCapacity(150.0).build();
        Battery battery2 = new BatteryBuilder().withPostcode("44600").withWattCapacity(200.0).build();
        Battery battery3 = new BatteryBuilder().withPostcode("44700").withWattCapacity(100.0).build();

        when(batteryRepository.findAll()).thenReturn(List.of(battery1, battery2, battery3));

        // Act
        List<PostcodeStatistics> statistics = batteryService.getStatisticsByPostcode();

        // Assert
        assertNotNull(statistics);
        assertEquals(2, statistics.size());

        PostcodeStatistics stats1 = statistics.get(0);
        assertEquals("44600", stats1.getPostcode());
        assertEquals(2, stats1.getBatteryCount());
        assertEquals(350.0, stats1.getTotalCapacity());
        assertEquals(175.0, stats1.getAverageCapacity());

        PostcodeStatistics stats2 = statistics.get(1);
        assertEquals("44700", stats2.getPostcode());
        assertEquals(1, stats2.getBatteryCount());
        assertEquals(100.0, stats2.getTotalCapacity());
        assertEquals(100.0, stats2.getAverageCapacity());

        verify(batteryRepository).findAll();
    }

//    @Test
//    void getBatteriesInPostcodeRange_ShouldReturnCorrectResponse() {
//        // Arrange
//        PostcodeRangeRequest request = new PostcodeRangeRequest();
//        request.setStartPostcode("4000");
//        request.setEndPostcode("5000");
//        request.setMinWattCapacity(100.0);
//        request.setMaxWattCapacity(300.0);
//
//        Battery battery1 = new BatteryBuilder().withName("Battery1").withPostcode("4500").withWattCapacity(150.0).build();
//        Battery battery2 = new BatteryBuilder().withName("Battery2").withPostcode("4600").withWattCapacity(200.0).build();
//
//        List<Battery> batteries = List.of(battery1, battery2);
//        Object[] statistics = new Object[]{2L, 350.0, 175.0, 150.0, 200.0};
//
//        when(batteryRepository.findBatteriesInPostcodeRangeWithCapacityFilter(
//                request.getStartPostcode(),
//                request.getEndPostcode(),
//                request.getMinWattCapacity(),
//                request.getMaxWattCapacity()
//        )).thenReturn(batteries);
//
//        when(batteryRepository.getBatteryStatisticsInPostcodeRangeWithCapacityFilter(
//                request.getStartPostcode(),
//                request.getEndPostcode(),
//                request.getMinWattCapacity(),
//                request.getMaxWattCapacity()
//        )).thenReturn(statistics);
//
//        // Act
//        PostcodeRangeResponse response = batteryService.getBatteriesInPostcodeRange(request);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(2, response.getBatteryNames().size());
//        assertEquals("Battery1", response.getBatteryNames().get(0));
//        assertEquals("Battery2", response.getBatteryNames().get(1));
//
//        PostcodeRangeResponse.BatteryStatistics stats = response.getStatistics();
//        assertNotNull(stats);
//        assertEquals(2, stats.getTotalBatteries());
//        assertEquals(350.0, stats.getTotalWattCapacity());
//        assertEquals(175.0, stats.getAverageWattCapacity());
//        assertEquals(150.0, stats.getMinWattCapacity());
//        assertEquals(200.0, stats.getMaxWattCapacity());
//
//        verify(batteryRepository).findBatteriesInPostcodeRangeWithCapacityFilter(
//                request.getStartPostcode(),
//                request.getEndPostcode(),
//                request.getMinWattCapacity(),
//                request.getMaxWattCapacity()
//        );
//        verify(batteryRepository).getBatteryStatisticsInPostcodeRangeWithCapacityFilter(
//                request.getStartPostcode(),
//                request.getEndPostcode(),
//                request.getMinWattCapacity(),
//                request.getMaxWattCapacity()
//        );
//    }

//    @Test
//    void getBatteriesInPostcodeRange_WithoutCapacityFilter_ShouldReturnCorrectResponse() {
//        // Arrange
//        PostcodeRangeRequest request = new PostcodeRangeRequest();
//        request.setStartPostcode("4000");
//        request.setEndPostcode("5000");
//
//        Battery battery1 = new BatteryBuilder().withName("Battery1").withPostcode("4500").withWattCapacity(150.0).build();
//        Battery battery2 = new BatteryBuilder().withName("Battery2").withPostcode("4600").withWattCapacity(200.0).build();
//
//        List<Battery> batteries = List.of(battery1, battery2);
//        Object[] statistics = new Object[]{2L, 350.0, 175.0, 150.0, 200.0};
//
//        when(batteryRepository.findBatteriesInPostcodeRange(
//                request.getStartPostcode(),
//                request.getEndPostcode()
//        )).thenReturn(batteries);
//
//        when(batteryRepository.getBatteryStatisticsInPostcodeRange(
//                request.getStartPostcode(),
//                request.getEndPostcode()
//        )).thenReturn(statistics);
//
//        // Act
//        PostcodeRangeResponse response = batteryService.getBatteriesInPostcodeRange(request);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(2, response.getBatteryNames().size());
//        assertEquals("Battery1", response.getBatteryNames().get(0));
//        assertEquals("Battery2", response.getBatteryNames().get(1));
//
//        PostcodeRangeResponse.BatteryStatistics stats = response.getStatistics();
//        assertNotNull(stats);
//        assertEquals(2, stats.getTotalBatteries());
//        assertEquals(350.0, stats.getTotalWattCapacity());
//        assertEquals(175.0, stats.getAverageWattCapacity());
//        assertEquals(150.0, stats.getMinWattCapacity());
//        assertEquals(200.0, stats.getMaxWattCapacity());
//
//        verify(batteryRepository).findBatteriesInPostcodeRange(
//                request.getStartPostcode(),
//                request.getEndPostcode()
//        );
//        verify(batteryRepository).getBatteryStatisticsInPostcodeRange(
//                request.getStartPostcode(),
//                request.getEndPostcode()
//        );
//    }
}