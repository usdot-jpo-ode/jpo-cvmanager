package com.trihydro.rsuinfobridge.service;

import com.trihydro.rsuinfobridge.models.tables.Rsu;
import com.trihydro.rsuinfobridge.repository.RsuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RsuService {
    private final RsuRepository rsuRepository;

    public List<Rsu> getAll(boolean timDepositEnabledOnly) {
        if (timDepositEnabledOnly) {
            return rsuRepository.findByRsuOptionTimDepositIsTrue();
        } else {
            return rsuRepository.findAll();
        }

    }
}