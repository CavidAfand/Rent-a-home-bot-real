package org.forbrightfuture.rentahomebot.service.broadcast;

import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastChatDTO;

import java.util.List;

public interface BroadcastSendService {

    void sendCustomBroadcast(String message, List<BroadcastChatDTO> chatList);

}
