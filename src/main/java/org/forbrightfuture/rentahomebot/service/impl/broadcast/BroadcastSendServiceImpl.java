package org.forbrightfuture.rentahomebot.service.impl.broadcast;

import org.forbrightfuture.rentahomebot.constants.ChatStage;
import org.forbrightfuture.rentahomebot.dto.telegram.send.ReplyKeyboardRemoveDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.send.text.SendMessageDTO;
import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastChatDTO;
import org.forbrightfuture.rentahomebot.service.TelegramMessagingService;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastSendService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BroadcastSendServiceImpl implements BroadcastSendService {

    private final TelegramMessagingService telegramMessagingService;

    public BroadcastSendServiceImpl(TelegramMessagingService telegramMessagingService) {
        this.telegramMessagingService = telegramMessagingService;
    }

    @Override
    public void sendCustomBroadcast(String message, List<BroadcastChatDTO> chatList) {
        for (BroadcastChatDTO broadcastChatDTO: chatList) {

            if (broadcastChatDTO.getChatStage() == ChatStage.BOT_BLOCKED)
                continue;

            SendMessageDTO sendMessageDTO = new SendMessageDTO(broadcastChatDTO.getChatId(),
                    message, new ReplyKeyboardRemoveDTO(false));

            telegramMessagingService.sendMessage(sendMessageDTO);
        }
    }
}
