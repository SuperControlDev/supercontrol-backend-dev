package com.supercontrol.backend.controller;

import com.supercontrol.backend.domain.Machine;
import com.supercontrol.backend.service.MachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    @GetMapping("/machines")
    public List<Machine> getMachines() {
        return machineService.getAllMachines();
    }
}
