package com.example.ticketing.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteOptionDTO {
    private List<TrainLegDTO> legs;
    private LocalDateTime totalDepartureTime;
    private LocalDateTime totalArrivalTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainLegDTO {
        private String trainNumber;
        private String fromStation;
        private String toStation;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
    }
}
