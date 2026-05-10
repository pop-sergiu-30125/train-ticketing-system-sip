package com.example.ticketing.services;

import com.example.ticketing.models.Booking;
import com.example.ticketing.models.Train;
import com.example.ticketing.repositories.BookingRepository;
import com.example.ticketing.repositories.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainService {

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Train reportDelay(Long trainId, int delayMinutes) {
        Train delayedTrain = trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found"));

        delayedTrain.setDelayMinutes(delayMinutes);
        Train updatedTrain = trainRepository.save(delayedTrain);

        // Notify customers
        List<Booking> bookings = bookingRepository.findByTrainId(trainId);
        for (Booking b : bookings) {
            checkAndNotify(b, updatedTrain, delayMinutes);
        }

        return updatedTrain;
    }

    private void checkAndNotify(Booking booking, Train delayedTrain, int delayMinutes) {

        List<Booking> userBookings = bookingRepository.findByUserEmail(booking.getUser().getEmail());
        
        Booking nextLeg = null;
        for (Booking other : userBookings) {
            if (other.getId().equals(booking.getId())) continue;
            
            if (other.getFromStation().getId().equals(booking.getToStation().getId())) {
                if (other.getTrain().getBaseDepartureTime().isAfter(delayedTrain.getBaseDepartureTime())) {
                    nextLeg = other;
                    break;
                }
            }
        }

        if (nextLeg != null) {
            java.time.LocalDateTime newArrivalAtTransfer = delayedTrain.getBaseDepartureTime()
                    .plusMinutes(getArrivalOffset(delayedTrain, booking.getToStation()))
                    .plusMinutes(delayMinutes);
            
            java.time.LocalDateTime nextLegDeparture = nextLeg.getTrain().getBaseDepartureTime()
                    .plusMinutes(getDepartureOffset(nextLeg.getTrain(), nextLeg.getFromStation()))
                    .plusMinutes(nextLeg.getTrain().getDelayMinutes());

            if (newArrivalAtTransfer.isAfter(nextLegDeparture)) {
                sendMissedConnectionEmail(booking, nextLeg, delayMinutes);
                return;
            }
        }

        sendDelayNotification(booking, delayMinutes);
    }

    private int getArrivalOffset(Train train, com.example.ticketing.models.Station station) {
        return train.getRoute().getRouteStations().stream()
                .filter(rs -> rs.getStation().getId().equals(station.getId()))
                .findFirst()
                .map(com.example.ticketing.models.RouteStation::getTravelTimeOffsetMinutes)
                .orElse(0);
    }

    private int getDepartureOffset(Train train, com.example.ticketing.models.Station station) {
        return getArrivalOffset(train, station); // Simplified: Arrival = Departure offset
    }

    private void sendMissedConnectionEmail(Booking current, Booking next, int delayMinutes) {
        String subject = "URGENT: Missed Connection for Train " + current.getTrain().getTrainNumber();
        String body = String.format(
                "Dear Customer,\n\nWe regret to inform you that due to a %d minute delay on train %s, " +
                "you will likely MISS your connecting train %s at %s.\n\n" +
                "New Estimated Arrival: %s\nConnecting Train Departs: %s\n\n" +
                "Please contact the station office for re-routing options.",
                delayMinutes,
                current.getTrain().getTrainNumber(),
                next.getTrain().getTrainNumber(),
                current.getToStation().getName(),
                current.getTrain().getBaseDepartureTime().plusMinutes(getArrivalOffset(current.getTrain(), current.getToStation())).plusMinutes(delayMinutes),
                next.getTrain().getBaseDepartureTime().plusMinutes(getDepartureOffset(next.getTrain(), next.getFromStation()))
        );
        emailService.sendEmail(current.getUser().getEmail(), subject, body);
    }

    private void sendDelayNotification(Booking booking, int delayMinutes) {
        String subject = "Train Delay Notification: " + booking.getTrain().getTrainNumber();
        String body = String.format(
                "Dear Customer,\n\nWe regret to inform you that train %s has encountered a delay of %d minutes.\n" +
                "Original Departure: %s\nNew estimated Departure: %s\n\nWe apologize for the inconvenience.",
                booking.getTrain().getTrainNumber(),
                delayMinutes,
                booking.getTrain().getBaseDepartureTime(),
                booking.getTrain().getBaseDepartureTime().plusMinutes(delayMinutes)
        );

        emailService.sendEmail(booking.getUser().getEmail(), subject, body);
    }
}
