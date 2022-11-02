package org.forbrightfuture.rentahomebot.service.impl.broadcast;

import lombok.extern.slf4j.Slf4j;
import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastMessageDTO;
import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastChatDTO;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastMessageService;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastSegmentService;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastSendService;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BroadcastServiceImpl implements BroadcastService {

    private final BroadcastMessageService broadcastMessageService;
    private final BroadcastSegmentService broadcastSegmentService;
    private final BroadcastSendService broadcastSendService;

    public BroadcastServiceImpl(BroadcastMessageService broadcastMessageService,
                                BroadcastSegmentService broadcastSegmentService,
                                BroadcastSendService broadcastSendService) {
        this.broadcastMessageService = broadcastMessageService;
        this.broadcastSegmentService = broadcastSegmentService;
        this.broadcastSendService = broadcastSendService;
    }

    @Override
    public boolean sendCustomBroadcastMessage() {
        boolean sendResult = false;

        BroadcastMessageDTO broadcastMessageDTO = broadcastMessageService.getNextBroadcastMessage();

        if (broadcastMessageDTO == null)
            return sendResult;

        List<BroadcastChatDTO> broadcastChatDTOList =
                broadcastSegmentService.getChatListBySegmentId(broadcastMessageDTO.getSegmentId());

        if (broadcastChatDTOList.size() > 0) {
            log.info("Broadcast " + broadcastMessageDTO.getBroadcastName() + " is started to send;");

            broadcastSendService.sendCustomBroadcast(broadcastMessageDTO.getBroadcastContent(), broadcastChatDTOList);
            broadcastMessageService.setBroadcastMessageAsSent(broadcastMessageDTO);

            log.info("Broadcast " + broadcastMessageDTO.getBroadcastName() + " sent stopped;");

            sendResult = true;
        }
        return sendResult;
    }
}
