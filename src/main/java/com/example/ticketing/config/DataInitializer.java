package com.example.ticketing.config;

import com.example.ticketing.models.Role;
import com.example.ticketing.models.User;
import com.example.ticketing.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.example.ticketing.repositories.StationRepository stationRepository;

    @Autowired
    private com.example.ticketing.repositories.TrainRouteRepository routeRepository;

    @Autowired
    private com.example.ticketing.repositories.TrainRepository trainRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
        if (stationRepository.count() == 0) {
            initializeTrainData();
        }
    }

    private void initializeUsers() {
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }

        if (userRepository.findByEmail("user@example.com").isEmpty()) {
            User user = User.builder()
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
        }
    }

    private void initializeTrainData() {
        com.example.ticketing.models.Station bm = stationRepository.save(new com.example.ticketing.models.Station(null, "Baia Mare"));
        com.example.ticketing.models.Station jibou = stationRepository.save(new com.example.ticketing.models.Station(null, "Jibou"));
        com.example.ticketing.models.Station dej = stationRepository.save(new com.example.ticketing.models.Station(null, "Dej Calatori"));
        com.example.ticketing.models.Station cluj = stationRepository.save(new com.example.ticketing.models.Station(null, "Cluj Napoca"));
        com.example.ticketing.models.Station beclean = stationRepository.save(new com.example.ticketing.models.Station(null, "Beclean pe Somes"));

        com.example.ticketing.models.TrainRoute r1 = com.example.ticketing.models.TrainRoute.builder().name("BM-CJ Direct").build();
        r1.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, r1, bm, 0, 0));
        r1.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, r1, jibou, 1, 60));
        r1.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, r1, dej, 2, 120));
        r1.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, r1, cluj, 3, 180));
        routeRepository.save(r1);

        com.example.ticketing.models.TrainRoute r2 = com.example.ticketing.models.TrainRoute.builder().name("BM-BEC").build();
        r2.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, r2, bm, 0, 0));
        r2.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, r2, beclean, 1, 140));
        routeRepository.save(r2);

        com.example.ticketing.models.TrainRoute r3 = com.example.ticketing.models.TrainRoute.builder().name("BEC-CJ").build();
        r3.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, r3, beclean, 0, 0));
        r3.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, r3, dej, 1, 30));
        r3.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, r3, cluj, 2, 90));
        routeRepository.save(r3);

        java.time.LocalDateTime today = java.time.LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);

        trainRepository.save(com.example.ticketing.models.Train.builder()
                .trainNumber("R4096")
                .route(r1)
                .capacity(50)
                .baseDepartureTime(today)
                .build());

        trainRepository.save(com.example.ticketing.models.Train.builder()
                .trainNumber("IR1746")
                .route(r1)
                .capacity(120)
                .baseDepartureTime(today.plusHours(6))
                .build());

        trainRepository.save(com.example.ticketing.models.Train.builder()
                .trainNumber("R4001")
                .route(r2)
                .capacity(40)
                .baseDepartureTime(today.plusHours(2))
                .build());

        trainRepository.save(com.example.ticketing.models.Train.builder()
                .trainNumber("IR1631")
                .route(r3)
                .capacity(100)
                .baseDepartureTime(today.plusHours(5))
                .build());

        com.example.ticketing.models.TrainRoute rDejCj = com.example.ticketing.models.TrainRoute.builder().name("Dej-CJ Short").build();
        rDejCj.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, rDejCj, dej, 0, 0));
        rDejCj.getRouteStations().add(new com.example.ticketing.models.RouteStation(null, rDejCj, cluj, 1, 60));
        routeRepository.save(rDejCj);

        trainRepository.save(com.example.ticketing.models.Train.builder()
                .trainNumber("R4008")
                .route(rDejCj)
                .capacity(30)
                .baseDepartureTime(today.plusHours(3)) 
                .build());
    }
}
