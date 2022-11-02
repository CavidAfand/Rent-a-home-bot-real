package org.forbrightfuture.rentahomebot.service.broadcast;

import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastChatDTO;

import java.util.List;

public interface BroadcastSegmentService {

    public List<BroadcastChatDTO> getChatListBySegmentId(int segmentId);

}
