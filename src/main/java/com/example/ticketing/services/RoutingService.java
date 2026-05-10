package com.example.ticketing.services;

import com.example.ticketing.dtos.RouteOptionDTO;
import com.example.ticketing.models.RouteStation;
import com.example.ticketing.models.Train;
import com.example.ticketing.repositories.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoutingService {

    @Autowired
    private TrainRepository trainRepository;

    public List<RouteOptionDTO> findRoutes(Long fromStationId, Long toStationId) {
        List<Train> allTrains = trainRepository.findAll();
        List<RouteOptionDTO> options = new ArrayList<>();

        for (Train train : allTrains) {
            List<RouteStation> rss = train.getRoute().getRouteStations();
            RouteStation fromRS = findInList(rss, fromStationId);
            RouteStation toRS = findInList(rss, toStationId);

            if (fromRS != null && toRS != null && fromRS.getStopOrder() < toRS.getStopOrder()) {
                options.add(createDirectOption(train, fromRS, toRS));
            }
        }


        for (Train t1 : allTrains) {
            List<RouteStation> rss1 = t1.getRoute().getRouteStations();
            RouteStation fromRS1 = findInList(rss1, fromStationId);
            if (fromRS1 == null) continue;

            for (RouteStation changeRS1 : rss1) {
                if (changeRS1.getStopOrder() <= fromRS1.getStopOrder()) continue;

                for (Train t2 : allTrains) {
                    if (t1.getId().equals(t2.getId())) continue;

                    List<RouteStation> rss2 = t2.getRoute().getRouteStations();
                    RouteStation changeRS2 = findInList(rss2, changeRS1.getStation().getId());
                    RouteStation toRS2 = findInList(rss2, toStationId);

                    if (changeRS2 != null && toRS2 != null && changeRS2.getStopOrder() < toRS2.getStopOrder()) {
                        LocalDateTime t1Arrival = getArrivalTime(t1, changeRS1);
                        LocalDateTime t2Departure = getDepartureTime(t2, changeRS2);

                        if (t1Arrival.isBefore(t2Departure)) {
                            options.add(createChangeoverOption(t1, fromRS1, changeRS1, t2, changeRS2, toRS2));
                        }
                    }
                }
            }
        }

        return options.stream()
                .sorted(Comparator.comparing(RouteOptionDTO::getTotalDepartureTime))
                .collect(Collectors.toList());
    }

    private RouteStation findInList(List<RouteStation> rss, Long stationId) {
        return rss.stream().filter(rs -> rs.getStation().getId().equals(stationId)).findFirst().orElse(null);
    }

    private LocalDateTime getDepartureTime(Train train, RouteStation rs) {
        return train.getBaseDepartureTime()
                .plusMinutes(rs.getTravelTimeOffsetMinutes())
                .plusMinutes(train.getDelayMinutes());
    }

    private LocalDateTime getArrivalTime(Train train, RouteStation rs) {
        return getDepartureTime(train, rs);
    }

    private RouteOptionDTO createDirectOption(Train train, RouteStation from, RouteStation to) {
        RouteOptionDTO.TrainLegDTO leg = RouteOptionDTO.TrainLegDTO.builder()
                .trainNumber(train.getTrainNumber())
                .fromStation(from.getStation().getName())
                .toStation(to.getStation().getName())
                .departureTime(getDepartureTime(train, from))
                .arrivalTime(getArrivalTime(train, to))
                .build();

        return RouteOptionDTO.builder()
                .legs(List.of(leg))
                .totalDepartureTime(leg.getDepartureTime())
                .totalArrivalTime(leg.getArrivalTime())
                .build();
    }

    private RouteOptionDTO createChangeoverOption(Train t1, RouteStation f1, RouteStation c1,
                                                 Train t2, RouteStation c2, RouteStation t2to) {
        RouteOptionDTO.TrainLegDTO leg1 = RouteOptionDTO.TrainLegDTO.builder()
                .trainNumber(t1.getTrainNumber())
                .fromStation(f1.getStation().getName())
                .toStation(c1.getStation().getName())
                .departureTime(getDepartureTime(t1, f1))
                .arrivalTime(getArrivalTime(t1, c1))
                .build();

        RouteOptionDTO.TrainLegDTO leg2 = RouteOptionDTO.TrainLegDTO.builder()
                .trainNumber(t2.getTrainNumber())
                .fromStation(c2.getStation().getName())
                .toStation(t2to.getStation().getName())
                .departureTime(getDepartureTime(t2, c2))
                .arrivalTime(getArrivalTime(t2, t2to))
                .build();

        return RouteOptionDTO.builder()
                .legs(List.of(leg1, leg2))
                .totalDepartureTime(leg1.getDepartureTime())
                .totalArrivalTime(leg2.getArrivalTime())
                .build();
    }
}
