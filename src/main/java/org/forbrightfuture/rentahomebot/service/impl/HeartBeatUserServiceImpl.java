package org.forbrightfuture.rentahomebot.service.impl;

import org.forbrightfuture.rentahomebot.dto.HeartBeatUserDTO;
import org.forbrightfuture.rentahomebot.entity.HeartBeatUser;
import org.forbrightfuture.rentahomebot.repository.HeartBeatUserRepository;
import org.forbrightfuture.rentahomebot.service.HeartBeatUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HeartBeatUserServiceImpl implements HeartBeatUserService {

    private final HeartBeatUserRepository heartBeatUserRepository;

    public HeartBeatUserServiceImpl(HeartBeatUserRepository heartBeatUserRepository) {
        this.heartBeatUserRepository = heartBeatUserRepository;
    }

    @Override
    public List<HeartBeatUserDTO> getAllHeartBeatUsers() {
        List<HeartBeatUser> heartBeatUsers = heartBeatUserRepository.findAll();
        List<HeartBeatUserDTO> heartBeatUserDTOs = new ArrayList<>();
        for(HeartBeatUser heartBeatUser: heartBeatUsers) {
            HeartBeatUserDTO heartBeatUserDTO = new HeartBeatUserDTO(heartBeatUser.getChatId());
            heartBeatUserDTOs.add(heartBeatUserDTO);
        }
        return heartBeatUserDTOs;
    }
}
