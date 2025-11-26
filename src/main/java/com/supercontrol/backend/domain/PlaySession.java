package com.supercontrol.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_L_PLAYSESSION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private Machine machine;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;
}
