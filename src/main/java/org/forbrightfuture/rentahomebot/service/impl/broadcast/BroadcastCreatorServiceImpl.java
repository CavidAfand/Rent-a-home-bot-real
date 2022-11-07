package org.forbrightfuture.rentahomebot.service.impl.broadcast;

import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastCreator;
import org.forbrightfuture.rentahomebot.repository.broadcast.BroadcastCreatorRepository;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastCreatorService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BroadcastCreatorServiceImpl implements BroadcastCreatorService {

    private final BroadcastCreatorRepository broadcastCreatorRepository;

    public BroadcastCreatorServiceImpl(BroadcastCreatorRepository broadcastCreatorRepository) {
        this.broadcastCreatorRepository = broadcastCreatorRepository;
    }

    @Override
    public boolean getBroadcastCreatorById(long chatId) {
        Optional<BroadcastCreator> broadcastCreatorOptional = broadcastCreatorRepository.findById(chatId);
        if (broadcastCreatorOptional.isPresent()) {
            return true;
        }
        return false;
    }
}
