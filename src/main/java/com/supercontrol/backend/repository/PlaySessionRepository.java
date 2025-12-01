package com.supercontrol.backend.repository;

import com.supercontrol.backend.domain.PlaySession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaySessionRepository extends JpaRepository<PlaySession, Long> {
}
