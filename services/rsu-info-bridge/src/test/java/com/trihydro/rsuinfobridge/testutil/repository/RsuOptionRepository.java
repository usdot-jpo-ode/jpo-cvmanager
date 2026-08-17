package com.trihydro.rsuinfobridge.testutil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trihydro.rsuinfobridge.models.tables.RsuOption;

@Repository
public interface RsuOptionRepository extends JpaRepository<RsuOption, Integer> {
}

