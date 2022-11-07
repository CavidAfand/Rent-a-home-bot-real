package org.forbrightfuture.rentahomebot.service.broadcast;

import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastMessageDTO;

public interface BroadcastMessageService {

    BroadcastMessageDTO getNextBroadcastMessage();

    void setBroadcastMessageAsSent(BroadcastMessageDTO broadcastMessageDTO);

    boolean saveBroadcastMessage(String text, long chatId);

    void saveBroadcastMessage(BroadcastMessageDTO broadcastMessageDTO);

}
