package com.example.ticketing.services;

import com.example.ticketing.models.*;
import com.example.ticketing.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public List<Booking> bookJourney(String email, List<Long> trainIds, List<Long> fromStationIds, List<Long> toStationIds, int ticketCount) {
        if (trainIds.size() != fromStationIds.size() || trainIds.size() != toStationIds.size()) {
            throw new RuntimeException("Invalid journey details");
        }

        List<Booking> journeyBookings = new ArrayList<>();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (int i = 0; i < trainIds.size(); i++) {
            Long trainId = trainIds.get(i);
            Long fromId = fromStationIds.get(i);
            Long toId = toStationIds.get(i);

            Train train = trainRepository.findById(trainId)
                    .orElseThrow(() -> new RuntimeException("Train not found: " + trainId));

            List<RouteStation> routeStations = train.getRoute().getRouteStations();
            RouteStation fromRS = findRouteStation(routeStations, fromId);
            RouteStation toRS = findRouteStation(routeStations, toId);

            if (fromRS.getStopOrder() >= toRS.getStopOrder()) {
                throw new RuntimeException("Invalid route on leg " + (i+1));
            }

            validateCapacity(train, routeStations, fromRS.getStopOrder(), toRS.getStopOrder(), ticketCount);

            Booking booking = Booking.builder()
                    .user(user)
                    .train(train)
                    .fromStation(fromRS.getStation())
                    .toStation(toRS.getStation())
                    .bookingTime(LocalDateTime.now())
                    .tickets(new ArrayList<>())
                    .build();

            for (int j = 0; j < ticketCount; j++) {
                Ticket ticket = Ticket.builder()
                        .ticketNumber(UUID.randomUUID().toString())
                        .booking(booking)
                        .build();
                booking.getTickets().add(ticket);
            }

            journeyBookings.add(bookingRepository.save(booking));
        }

        sendJourneyConfirmationEmail(user, journeyBookings);

        return journeyBookings;
    }

    private void sendJourneyConfirmationEmail(User user, List<Booking> bookings) {
        StringBuilder body = new StringBuilder();
        body.append("Dear Customer,\n\n");
        body.append("Your journey booking is confirmed. Details:\n\n");
        
        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            body.append("Leg ").append(i + 1).append(": ").append(b.getTrain().getTrainNumber()).append("\n");
            body.append("   From: ").append(b.getFromStation().getName()).append("\n");
            body.append("   To: ").append(b.getToStation().getName()).append("\n");
            body.append("   Departure: ").append(b.getTrain().getActualDepartureTime()).append("\n\n");
        }

        body.append("Tickets are attached to your account profile.\n");
        body.append("Thank you for choosing us!");

        emailService.sendEmail(user.getEmail(), "Journey Confirmation", body.toString());
    }


    public List<Booking> getBookingsByTrainId(Long trainId) {
        return bookingRepository.findByTrainId(trainId);
    }

    private void validateCapacity(Train train, List<RouteStation> routeStations, int fromOrder, int toOrder, int requestedCount) {
        List<Booking> existingBookings = bookingRepository.findByTrainId(train.getId());

        for (int i = fromOrder; i < toOrder; i++) {
            int currentPassengers = 0;
            for (Booking b : existingBookings) {
                int bFromOrder = findRouteStation(routeStations, b.getFromStation().getId()).getStopOrder();
                int bToOrder = findRouteStation(routeStations, b.getToStation().getId()).getStopOrder();

                if (bFromOrder <= i && bToOrder >= i + 1) {
                    currentPassengers += b.getTickets().size();
                }
            }

            if (currentPassengers + requestedCount > train.getCapacity()) {
                throw new RuntimeException("Overbooked for segment starting at " + 
                    getStationNameAtOrder(routeStations, i));
            }
        }
    }

    private RouteStation findRouteStation(List<RouteStation> routeStations, Long stationId) {
        return routeStations.stream()
                .filter(rs -> rs.getStation().getId().equals(stationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Station not found on this route"));
    }

    private String getStationNameAtOrder(List<RouteStation> routeStations, int order) {
        return routeStations.stream()
                .filter(rs -> rs.getStopOrder() == order)
                .findFirst()
                .map(rs -> rs.getStation().getName())
                .orElse("Unknown Station");
    }
}
