package com.trihydro.rsuinfobridge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.trihydro.rsuinfobridge.models.tables.Rsu;

import java.util.List;

@Repository
public interface RsuRepository extends JpaRepository<Rsu, Integer> {
    List<Rsu> findByRsuOptionTimDepositIsTrue();
}
