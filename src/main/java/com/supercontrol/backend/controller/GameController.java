package com.supercontrol.backend.controller;

import com.supercontrol.backend.dto.GameStartRequest;
import com.supercontrol.backend.dto.GameStartResponse;
import com.supercontrol.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/start")
    public GameStartResponse startGame(@RequestBody GameStartRequest request) {

        System.out.println("🔥 GameController /start 호출됨");
        System.out.println("🔥 유저: " + request.getUserId() + ", 머신: " + request.getMachineId());

        return gameService.startGame(request);
    }
}
