package org.forbrightfuture.rentahomebot.controller;

import org.forbrightfuture.rentahomebot.dto.HeartBeatResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeartBeatController {

    @GetMapping("/heartbeat")
    public HeartBeatResponseDTO getHeartBeat() {
        HeartBeatResponseDTO heartBeatResponseDTO = new HeartBeatResponseDTO();
        heartBeatResponseDTO.setStatus("UP");
        return heartBeatResponseDTO;
    }


}
