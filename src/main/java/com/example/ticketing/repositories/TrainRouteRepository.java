package com.example.ticketing.repositories;

import com.example.ticketing.models.TrainRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainRouteRepository extends JpaRepository<TrainRoute, Long> {
}
