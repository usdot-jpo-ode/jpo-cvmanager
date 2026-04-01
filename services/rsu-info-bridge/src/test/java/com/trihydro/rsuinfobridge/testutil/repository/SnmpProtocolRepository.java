package com.trihydro.rsuinfobridge.testutil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trihydro.rsuinfobridge.models.tables.SnmpProtocol;

@Repository
public interface SnmpProtocolRepository extends JpaRepository<SnmpProtocol, Integer> {
}

