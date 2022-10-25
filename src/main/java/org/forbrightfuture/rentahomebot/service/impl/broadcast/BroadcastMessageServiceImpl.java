package org.forbrightfuture.rentahomebot.service.impl.broadcast;

import org.forbrightfuture.rentahomebot.repository.broadcast.BroadcastMessageRepository;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastMessageService;
import org.springframework.stereotype.Service;

@Service
public class BroadcastMessageServiceImpl implements BroadcastMessageService {

    private final BroadcastMessageRepository broadcastMessageRepository;

    public BroadcastMessageServiceImpl(BroadcastMessageRepository broadcastMessageRepository) {
        this.broadcastMessageRepository = broadcastMessageRepository;
    }



}
