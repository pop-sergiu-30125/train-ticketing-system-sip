package com.example.ticketing.controllers;

import com.example.ticketing.dtos.RouteOptionDTO;
import com.example.ticketing.models.Booking;
import com.example.ticketing.services.BookingService;
import com.example.ticketing.services.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoutingService routingService;

    @GetMapping("/search")
    public List<RouteOptionDTO> searchRoutes(@RequestParam Long fromStationId, @RequestParam Long toStationId) {
        List<RouteOptionDTO> routes = routingService.findRoutes(fromStationId, toStationId);
        if (routes.isEmpty()) {
            throw new RuntimeException("No possible link found between the stations.");
        }
        return routes;
    }

    @PostMapping
    public List<Booking> bookJourney(java.security.Principal principal, 
                                    @RequestParam List<Long> trainIds, 
                                    @RequestParam List<Long> fromStationIds, 
                                    @RequestParam List<Long> toStationIds, 
                                    @RequestParam int count) {
        return bookingService.bookJourney(principal.getName(), trainIds, fromStationIds, toStationIds, count);
    }

    @GetMapping("/train/{trainId}")
    public List<Booking> getBookingsByTrain(@PathVariable Long trainId) {
        return bookingService.getBookingsByTrainId(trainId);
    }
}
