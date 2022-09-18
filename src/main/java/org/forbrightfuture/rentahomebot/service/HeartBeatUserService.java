package org.forbrightfuture.rentahomebot.service;

import org.forbrightfuture.rentahomebot.dto.HeartBeatUserDTO;

import java.util.List;

public interface HeartBeatUserService {

    List<HeartBeatUserDTO> getAllHeartBeatUsers();

}