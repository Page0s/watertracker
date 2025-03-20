package com.gograffer.watertracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.AppUser;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByUser(AppUser user);
    List<Location> findByUserId(Long userId);
}
