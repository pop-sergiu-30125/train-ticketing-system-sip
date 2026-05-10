package com.example.ticketing.controllers;

import com.example.ticketing.models.TrainRoute;
import com.example.ticketing.repositories.TrainRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class StationRouteController {

    @Autowired
    private TrainRouteRepository routeRepository;

    @GetMapping
    public List<TrainRoute> getAllRoutes() {
        return routeRepository.findAll();
    }

    @GetMapping("/{id}")
    public TrainRoute getRouteById(@PathVariable Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));
    }

    @PostMapping
    public TrainRoute createRoute(@RequestBody TrainRoute route) {
        if (route.getRouteStations() != null) {
            route.getRouteStations().forEach(rs -> rs.setRoute(route));
        }
        return routeRepository.save(route);
    }

    @PutMapping("/{id}")
    public TrainRoute updateRoute(@PathVariable Long id, @RequestBody TrainRoute routeDetails) {
        TrainRoute route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        route.setName(routeDetails.getName());
        
        if (routeDetails.getRouteStations() != null) {
            route.getRouteStations().clear();
            routeDetails.getRouteStations().forEach(rs -> {
                rs.setRoute(route);
                route.getRouteStations().add(rs);
            });
        }
        
        return routeRepository.save(route);
    }

    @DeleteMapping("/{id}")
    public void deleteRoute(@PathVariable Long id) {
        routeRepository.deleteById(id);
    }
}
