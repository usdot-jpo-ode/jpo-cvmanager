package com.trihydro.rsuinfobridge.testutil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trihydro.rsuinfobridge.models.tables.RsuIntersection;

@Repository
public interface RsuIntersectionRepository extends JpaRepository<RsuIntersection, Integer> {
}

