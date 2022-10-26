package org.forbrightfuture.rentahomebot.service.impl.broadcast;

import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastMessageDTO;
import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastMessage;
import org.forbrightfuture.rentahomebot.repository.broadcast.BroadcastMessageRepository;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastMessageService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BroadcastMessageServiceImpl implements BroadcastMessageService {

    private final BroadcastMessageRepository broadcastMessageRepository;

    public BroadcastMessageServiceImpl(BroadcastMessageRepository broadcastMessageRepository) {
        this.broadcastMessageRepository = broadcastMessageRepository;
    }


    @Override
    public BroadcastMessageDTO getNextBroadcastMessage() {
        List<BroadcastMessage> broadcastMessages = broadcastMessageRepository.getBroadcastMessageByFirstNotSent(Pageable.ofSize(1));
        if (broadcastMessages != null && broadcastMessages.size() > 0)
            return new BroadcastMessageDTO(broadcastMessages.get(0));
        return null;
    }
}
