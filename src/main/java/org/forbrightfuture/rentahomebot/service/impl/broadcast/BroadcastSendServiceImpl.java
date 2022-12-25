package org.forbrightfuture.rentahomebot.service.impl.broadcast;

import lombok.extern.slf4j.Slf4j;
import org.forbrightfuture.rentahomebot.constants.ChatStage;
import org.forbrightfuture.rentahomebot.dto.telegram.send.ReplyKeyboardRemoveDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.send.text.SendTextDTO;
import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastChatDTO;
import org.forbrightfuture.rentahomebot.service.TelegramMessagingService;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastSendService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BroadcastSendServiceImpl implements BroadcastSendService {

    private final TelegramMessagingService telegramMessagingService;

    public BroadcastSendServiceImpl(TelegramMessagingService telegramMessagingService) {
        this.telegramMessagingService = telegramMessagingService;
    }

    @Override
    public void sendCustomBroadcast(String message, List<BroadcastChatDTO> chatList) {
        long successfulCount = 0;
        long unsuccessfulCount = 0;
        for (BroadcastChatDTO broadcastChatDTO: chatList) {

            if (broadcastChatDTO.getChatStage() == ChatStage.BOT_BLOCKED)
                continue;

            SendTextDTO sendTextDTO = new SendTextDTO(broadcastChatDTO.getChatId(),
                    message, new ReplyKeyboardRemoveDTO(false));

            boolean isSuccessful = telegramMessagingService.sendMessage(sendTextDTO);

            if (isSuccessful)
                successfulCount++;
            else
                unsuccessfulCount++;
        }

        log.info("Broadcast was sent to " + successfulCount + " chat successfully. " + unsuccessfulCount + " chat was unsuccessful.");
    }
}
