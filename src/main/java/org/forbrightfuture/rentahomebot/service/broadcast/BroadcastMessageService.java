package org.forbrightfuture.rentahomebot.service.broadcast;

import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastMessageDTO;

public interface BroadcastMessageService {

    public BroadcastMessageDTO getNextBroadcastMessage();

}
