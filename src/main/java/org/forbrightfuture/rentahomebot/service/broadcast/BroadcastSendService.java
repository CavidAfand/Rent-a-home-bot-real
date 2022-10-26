package org.forbrightfuture.rentahomebot.service.broadcast;

import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastChatDTO;

import java.util.List;

public interface BroadcastSendService {

    public void sendCustomBroadcast(String message, List<BroadcastChatDTO> chatList);

}
