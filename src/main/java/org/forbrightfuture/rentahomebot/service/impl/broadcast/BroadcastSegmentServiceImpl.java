package org.forbrightfuture.rentahomebot.service.impl.broadcast;

import org.forbrightfuture.rentahomebot.entity.Chat;
import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastChatDTO;
import org.forbrightfuture.rentahomebot.repository.broadcast.BroadcastSegmentRepository;
import org.forbrightfuture.rentahomebot.service.ChatDataService;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastSegmentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BroadcastSegmentServiceImpl implements BroadcastSegmentService {

    private final BroadcastSegmentRepository broadcastSegmentRepository;
    private final ChatDataService chatDataService;

    public BroadcastSegmentServiceImpl(BroadcastSegmentRepository broadcastSegmentRepository,
                                       ChatDataService chatDataService) {
        this.broadcastSegmentRepository = broadcastSegmentRepository;
        this.chatDataService = chatDataService;
    }


    @Override
    public List<BroadcastChatDTO> getChatListBySegmentId(int segmentId) {
        List<BroadcastChatDTO> chatDTOList = new ArrayList<>();
        if (segmentId == 0) {
            List<Chat> allChats = chatDataService.getAllChat();
            for (Chat chat: allChats) {
                chatDTOList.add(new BroadcastChatDTO(chat));
            }
        }
        else {
            List<Chat> chatList = broadcastSegmentRepository.getChatIdListBySegmentId(segmentId);
            for (Chat chat : chatList) {
                chatDTOList.add(new BroadcastChatDTO(chat));
            }
        }
        return chatDTOList;
    }

}
