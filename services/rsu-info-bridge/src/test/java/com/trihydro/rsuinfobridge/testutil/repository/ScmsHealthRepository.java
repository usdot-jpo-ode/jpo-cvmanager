package com.trihydro.rsuinfobridge.testutil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trihydro.rsuinfobridge.testutil.models.ScmsHealth;

@Repository
public interface ScmsHealthRepository extends JpaRepository<ScmsHealth, Integer> {
}
