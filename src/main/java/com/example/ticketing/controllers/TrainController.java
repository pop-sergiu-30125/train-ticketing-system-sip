package com.example.ticketing.controllers;

import com.example.ticketing.models.Train;
import com.example.ticketing.repositories.TrainRepository;
import com.example.ticketing.services.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trains")
public class TrainController {

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private TrainService trainService;

    @GetMapping
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @GetMapping("/{id}")
    public Train getTrainById(@PathVariable Long id) {
        return trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));
    }

    @PostMapping
    public Train createTrain(@RequestBody Train train) {
        return trainRepository.save(train);
    }

    @PutMapping("/{id}")
    public Train updateTrain(@PathVariable Long id, @RequestBody Train trainDetails) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));

        train.setTrainNumber(trainDetails.getTrainNumber());
        train.setCapacity(trainDetails.getCapacity());
        train.setBaseDepartureTime(trainDetails.getBaseDepartureTime());
        if (trainDetails.getRoute() != null) {
            train.setRoute(trainDetails.getRoute());
        }

        return trainRepository.save(train);
    }

    @DeleteMapping("/{id}")
    public void deleteTrain(@PathVariable Long id) {
        trainRepository.deleteById(id);
    }

    @PostMapping("/{id}/delay")
    public Train reportDelay(@PathVariable Long id, @RequestParam int minutes) {
        return trainService.reportDelay(id, minutes);
    }
}
