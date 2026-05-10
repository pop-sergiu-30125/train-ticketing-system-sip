package com.example.ticketing.repositories;

import com.example.ticketing.models.RouteStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteStationRepository extends JpaRepository<RouteStation, Long> {
    List<RouteStation> findByRouteId(Long routeId);
}
