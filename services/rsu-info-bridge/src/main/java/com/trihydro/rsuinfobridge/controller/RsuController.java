package com.trihydro.rsuinfobridge.controller;

import com.trihydro.rsuinfobridge.models.dtos.RsuDto;
import com.trihydro.rsuinfobridge.service.RsuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rsus")
@RequiredArgsConstructor
public class RsuController {
    private final RsuService rsuService;

    @GetMapping
    public List<RsuDto> getAll(@RequestParam(defaultValue = "false") boolean timDepositEnabled) {
        return rsuService.getAll(timDepositEnabled);
    }
}
