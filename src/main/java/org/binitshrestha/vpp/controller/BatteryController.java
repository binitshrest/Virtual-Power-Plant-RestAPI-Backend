package org.binitshrestha.vpp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.binitshrestha.vpp.model.Battery;
import org.binitshrestha.vpp.model.PostcodeStatistics;
import org.binitshrestha.vpp.model.dto.*;
import org.binitshrestha.vpp.service.BatteryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batteries")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Battery Management", description = "Virtual Power Plant Battery Management API")
public class BatteryController {

    private final BatteryService batteryService;

    @PostMapping
    @Operation(summary = "Register a new battery", description = "Register a single battery in the virtual power plant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Battery successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid battery data provided"),
            @ApiResponse(responseCode = "409", description = "Battery with the same name already exists")
    })
    public ResponseEntity<BatteryResponse> registerBattery(
            @Valid @RequestBody BatteryCreateRequest request) {
        log.info("REST: Registering new battery with name: {}", request.getName());

        BatteryResponse response = batteryService.registerBattery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/batch")
    @Operation(summary = "Register multiple batteries", description = "Register multiple batteries concurrently in batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Batteries successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid battery data provided"),
            @ApiResponse(responseCode = "409", description = "One or more batteries already exist")
    })
    public ResponseEntity<List<BatteryResponse>> registerBatteriesBatch(
            @Valid @RequestBody BatteryBatchCreateRequest request) {
        log.info("REST: Registering {} batteries in batch", request.getBatteries().size());

        List<BatteryResponse> responses = batteryService.registerBatteriesBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping
    @Operation(summary = "Get all batteries", description = "Retrieve all registered batteries sorted by name")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all batteries")
    public ResponseEntity<List<BatteryResponse>> getAllBatteries() {
        log.info("REST: Retrieving all batteries");

        List<BatteryResponse> batteries = batteryService.getAllBatteries();
        return ResponseEntity.ok(batteries);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get battery by ID", description = "Retrieve a specific battery by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Battery found and returned"),
            @ApiResponse(responseCode = "404", description = "Battery not found")
    })
    public ResponseEntity<BatteryResponse> getBatteryById(
            @Parameter(description = "Battery ID", required = true)
            @PathVariable @NotNull @Min(1) Long id) {
        log.info("REST: Retrieving battery with ID: {}", id);

        BatteryResponse battery = batteryService.getBatteryById(id);
        return ResponseEntity.ok(battery);
    }

    @GetMapping("/postcode-range")
    @Operation(summary = "Get batteries in postcode range",
            description = "Retrieve batteries within a postcode range with optional capacity filters and statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved batteries in range"),
            @ApiResponse(responseCode = "400", description = "Invalid postcode range or capacity filters")
    })
    public ResponseEntity<PostcodeRangeResponse> getBatteriesInPostcodeRange(
            @Parameter(description = "Start postcode (inclusive)", required = true)
            @RequestParam String startPostcode,

            @Parameter(description = "End postcode (inclusive)", required = true)
            @RequestParam String endPostcode,

            @Parameter(description = "Minimum watt capacity filter (optional)")
            @RequestParam(required = false) Double minWattCapacity,

            @Parameter(description = "Maximum watt capacity filter (optional)")
            @RequestParam(required = false) Double maxWattCapacity) {

        log.info("REST: Querying batteries in postcode range: {} to {} with capacity filters: min={}, max={}",
                startPostcode, endPostcode, minWattCapacity, maxWattCapacity);

        PostcodeRangeRequest request = new PostcodeRangeRequest(
                startPostcode, endPostcode, minWattCapacity, maxWattCapacity);

        PostcodeRangeResponse response = batteryService.getBatteriesInPostcodeRange(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update battery status", description = "Update the operational status of a battery")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Battery status successfully updated"),
            @ApiResponse(responseCode = "404", description = "Battery not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status provided")
    })
    public ResponseEntity<BatteryResponse> updateBatteryStatus(
            @Parameter(description = "Battery ID", required = true)
            @PathVariable @NotNull @Min(1) Long id,

            @Parameter(description = "New battery status", required = true)
            @RequestParam Battery.BatteryStatus status) {

        log.info("REST: Updating battery {} status to {}", id, status);

        BatteryResponse response = batteryService.updateBatteryStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/high-capacity")
    @Operation(summary = "Get high capacity batteries",
            description = "Retrieve batteries with capacity greater than or equal to specified minimum")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved high capacity batteries")
    public ResponseEntity<List<BatteryResponse>> getHighCapacityBatteries(
            @Parameter(description = "Minimum capacity threshold", required = true)
            @RequestParam @NotNull @Min(0) Double minCapacity) {

        log.info("REST: Retrieving high capacity batteries with min capacity: {}", minCapacity);

        List<BatteryResponse> batteries = batteryService.getHighCapacityBatteries(minCapacity);
        return ResponseEntity.ok(batteries);
    }

    @GetMapping("/statistics/by-postcode")
    @Operation(summary = "Get statistics by postcode",
            description = "Retrieve aggregated battery statistics grouped by postcode")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved postcode statistics")
    public ResponseEntity<List<PostcodeStatistics>> getStatisticsByPostcode() {
        log.info("REST: Retrieving statistics by postcode");

        List<PostcodeStatistics> statistics = batteryService.getStatisticsByPostcode();
        return ResponseEntity.ok(statistics);
    }

    // Additional utility endpoints

    @GetMapping("/count")
    @Operation(summary = "Get total battery count", description = "Get the total number of registered batteries")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved battery count")
    public ResponseEntity<BatteryCountResponse> getTotalBatteryCount() {
        log.info("REST: Retrieving total battery count");

        List<BatteryResponse> allBatteries = batteryService.getAllBatteries();
        BatteryCountResponse response = new BatteryCountResponse(allBatteries.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search batteries by name", description = "Search batteries by name pattern (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching batteries")
    public ResponseEntity<List<BatteryResponse>> searchBatteriesByName(
            @Parameter(description = "Name pattern to search for", required = true)
            @RequestParam String namePattern) {

        log.info("REST: Searching batteries by name pattern: {}", namePattern);

        List<BatteryResponse> allBatteries = batteryService.getAllBatteries();
        List<BatteryResponse> matchingBatteries = allBatteries.stream()
                .filter(battery -> battery.getName().toLowerCase().contains(namePattern.toLowerCase()))
                .toList();

        return ResponseEntity.ok(matchingBatteries);
    }

    @GetMapping("/postcodes")
    @Operation(summary = "Get unique postcodes", description = "Retrieve all unique postcodes from registered batteries")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved unique postcodes")
    public ResponseEntity<List<String>> getUniquePostcodes() {
        log.info("REST: Retrieving unique postcodes");

        List<BatteryResponse> allBatteries = batteryService.getAllBatteries();
        List<String> uniquePostcodes = allBatteries.stream()
                .map(BatteryResponse::getPostcode)
                .distinct()
                .sorted()
                .toList();

        return ResponseEntity.ok(uniquePostcodes);
    }

    // Health check endpoint
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the battery service is operational")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<HealthCheckResponse> healthCheck() {
        try {
            int totalBatteries = batteryService.getAllBatteries().size();
            HealthCheckResponse response = new HealthCheckResponse(
                    "UP",
                    "Battery service is operational",
                    totalBatteries);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Health check failed", e);
            HealthCheckResponse response = new HealthCheckResponse(
                    "DOWN",
                    "Battery service is experiencing issues: " + e.getMessage(),
                    0);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
