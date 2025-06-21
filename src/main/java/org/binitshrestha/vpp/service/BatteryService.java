package org.binitshrestha.vpp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.binitshrestha.vpp.exception.BatteryAlreadyExistsException;
import org.binitshrestha.vpp.exception.BatteryNotFoundException;
import org.binitshrestha.vpp.model.Battery;
import org.binitshrestha.vpp.model.PostcodeStatistics;
import org.binitshrestha.vpp.model.dto.*;
import org.binitshrestha.vpp.repository.BatteryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatteryService {

    private final BatteryRepository batteryRepository;

    /**
     * Register a single battery
     */
    @Transactional
    public BatteryResponse registerBattery(BatteryCreateRequest request) {
        log.info("Registering battery with name: {}", request.getName());

        // Check if battery already exists
        if (batteryRepository.existsByName(request.getName())) {
            log.warn("Battery with name {} already exists", request.getName());
            throw new BatteryAlreadyExistsException("Battery with name '" + request.getName() + "' already exists");
        }

        Battery battery = mapToEntity(request);
        Battery savedBattery = batteryRepository.save(battery);

        log.info("Successfully registered battery with ID: {}", savedBattery.getId());
        return mapToResponse(savedBattery);
    }

    /**
     * Register multiple batteries concurrently
     */
    @Transactional
    public List<BatteryResponse> registerBatteriesBatch(BatteryBatchCreateRequest request) {
        log.info("Registering {} batteries in batch", request.getBatteries().size());

        // Using Java Streams for parallel processing and validation
        List<CompletableFuture<BatteryResponse>> futures = request.getBatteries()
                .parallelStream()
                .map(this::registerBatteryAsync)
                .collect(Collectors.toList());

        // Collect all results
        List<BatteryResponse> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        log.info("Successfully registered {} batteries", results.size());
        return results;
    }

    /**
     * Get batteries within postcode range with optional capacity filters
     */
    @Transactional(readOnly = true)
    public PostcodeRangeResponse getBatteriesInPostcodeRange(PostcodeRangeRequest request) {
        log.info("Querying batteries in postcode range: {} to {}",
                request.getStartPostcode(), request.getEndPostcode());

        List<Battery> batteries;
        Object[] statistics;

        if (request.getMinWattCapacity() != null || request.getMaxWattCapacity() != null) {
            batteries = batteryRepository.findBatteriesInPostcodeRangeWithCapacityFilter(
                    request.getStartPostcode(),
                    request.getEndPostcode(),
                    request.getMinWattCapacity(),
                    request.getMaxWattCapacity()
            );
            statistics = batteryRepository.getBatteryStatisticsInPostcodeRangeWithCapacityFilter(
                    request.getStartPostcode(),
                    request.getEndPostcode(),
                    request.getMinWattCapacity(),
                    request.getMaxWattCapacity()
            );
        } else {
            batteries = batteryRepository.findBatteriesInPostcodeRange(
                    request.getStartPostcode(),
                    request.getEndPostcode()
            );
            statistics = batteryRepository.getBatteryStatisticsInPostcodeRange(
                    request.getStartPostcode(),
                    request.getEndPostcode()
            );
        }

        // Using Java Streams to extract battery names and sort them
        List<String> batteryNames = batteries.stream()
                .map(Battery::getName)
                .sorted()
                .collect(Collectors.toList());

        PostcodeRangeResponse.BatteryStatistics batteryStats = createStatistics(statistics);

        log.info("Found {} batteries in postcode range", batteryNames.size());
        return new PostcodeRangeResponse(batteryNames, batteryStats);
    }

    /**
     * Get all batteries with filtering and sorting capabilities
     */
    @Transactional(readOnly = true)
    public List<BatteryResponse> getAllBatteries() {
        log.info("Retrieving all batteries");

        return batteryRepository.findAll().stream()
                .map(this::mapToResponse)
                .sorted((b1, b2) -> b1.getName().compareToIgnoreCase(b2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Get battery by ID
     */
    @Transactional(readOnly = true)
    public BatteryResponse getBatteryById(Long id) {
        log.info("Retrieving battery with ID: {}", id);

        Battery battery = batteryRepository.findById(id)
                .orElseThrow(() -> new BatteryNotFoundException("Battery with ID " + id + " not found"));

        return mapToResponse(battery);
    }

    /**
     * Update battery status
     */
    @Transactional
    public BatteryResponse updateBatteryStatus(Long id, Battery.BatteryStatus status) {
        log.info("Updating battery {} status to {}", id, status);

        Battery battery = batteryRepository.findById(id)
                .orElseThrow(() -> new BatteryNotFoundException("Battery with ID " + id + " not found"));

        battery.setStatus(status);
        Battery updatedBattery = batteryRepository.save(battery);

        log.info("Successfully updated battery status");
        return mapToResponse(updatedBattery);
    }

    /**
     * Get batteries by high capacity (demonstration of additional Java Streams usage)
     */
    @Transactional(readOnly = true)
    public List<BatteryResponse> getHighCapacityBatteries(Double minCapacity) {
        log.info("Retrieving batteries with capacity >= {}", minCapacity);

        return batteryRepository.findAll().stream()
                .filter(battery -> battery.getWattCapacity() >= minCapacity)
                .sorted((b1, b2) -> Double.compare(b2.getWattCapacity(), b1.getWattCapacity()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get aggregated statistics by postcode (demonstration of advanced Java Streams)
     */
    @Transactional(readOnly = true)
    public List<PostcodeStatistics> getStatisticsByPostcode() {
        log.info("Calculating statistics by postcode");

        return batteryRepository.findAll().stream()
                .collect(Collectors.groupingBy(Battery::getPostcode))
                .entrySet().stream()
                .map(entry -> {
                    String postcode = entry.getKey();
                    List<Battery> batteries = entry.getValue();

                    double totalCapacity = batteries.stream()
                            .mapToDouble(Battery::getWattCapacity)
                            .sum();

                    double avgCapacity = batteries.stream()
                            .mapToDouble(Battery::getWattCapacity)
                            .average()
                            .orElse(0.0);

                    return new PostcodeStatistics(
                            postcode,
                            batteries.size(),
                            totalCapacity,
                            avgCapacity
                    );
                })
                .sorted((s1, s2) -> s1.getPostcode().compareToIgnoreCase(s2.getPostcode()))
                .collect(Collectors.toList());
    }

    // Private helper methods
    private CompletableFuture<BatteryResponse> registerBatteryAsync(BatteryCreateRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return registerBattery(request);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    private Battery mapToEntity(BatteryCreateRequest request) {
        Battery battery = new Battery();
        battery.setName(request.getName());
        battery.setPostcode(request.getPostcode());
        battery.setWattCapacity(request.getWattCapacity());
        return battery;
    }

    private BatteryResponse mapToResponse(Battery battery) {
        return new BatteryResponse(
                battery.getId(),
                battery.getName(),
                battery.getPostcode(),
                battery.getWattCapacity(),
                battery.getStatus().name()
        );
    }

    private PostcodeRangeResponse.BatteryStatistics createStatistics(Object[] stats) {
        if (stats == null || stats.length == 0 || stats[0] == null) {
            return new PostcodeRangeResponse.BatteryStatistics(0, 0.0, 0.0, 0.0, 0.0);
        }

        Long count = (Long) stats[0];
        Double totalCapacity = (Double) stats[1];
        Double avgCapacity = (Double) stats[2];
        Double minCapacity = (Double) stats[3];
        Double maxCapacity = (Double) stats[4];

        return new PostcodeRangeResponse.BatteryStatistics(
                count.intValue(),
                totalCapacity != null ? totalCapacity : 0.0,
                avgCapacity != null ? avgCapacity : 0.0,
                minCapacity != null ? minCapacity : 0.0,
                maxCapacity != null ? maxCapacity : 0.0
        );
    }
}
