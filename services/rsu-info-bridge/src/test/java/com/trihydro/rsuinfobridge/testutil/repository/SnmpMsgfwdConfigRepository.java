package com.trihydro.rsuinfobridge.testutil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trihydro.rsuinfobridge.models.tables.SnmpMsgfwdConfig;
import com.trihydro.rsuinfobridge.models.tables.SnmpMsgfwdConfigId;

@Repository
public interface SnmpMsgfwdConfigRepository extends JpaRepository<SnmpMsgfwdConfig, SnmpMsgfwdConfigId> {
}

