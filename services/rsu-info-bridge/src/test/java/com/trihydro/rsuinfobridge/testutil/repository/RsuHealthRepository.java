package com.trihydro.rsuinfobridge.testutil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trihydro.rsuinfobridge.testutil.models.RsuHealth;

@Repository
public interface RsuHealthRepository extends JpaRepository<RsuHealth, Integer> {
}
