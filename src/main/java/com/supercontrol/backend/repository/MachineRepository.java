package com.supercontrol.backend.repository;

import com.supercontrol.backend.domain.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineRepository extends JpaRepository<Machine, Long> {
}
