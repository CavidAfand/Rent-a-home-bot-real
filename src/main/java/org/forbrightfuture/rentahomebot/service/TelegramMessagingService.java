package org.forbrightfuture.rentahomebot.service;

import org.forbrightfuture.rentahomebot.dto.telegram.send.TelegramSendMessage;
import org.forbrightfuture.rentahomebot.dto.telegram.send.photo.SendPhotoDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.send.text.SendTextDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.send.SendMessageResponseDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.update.TelegramUpdateDTO;
import org.forbrightfuture.rentahomebot.entity.Home;

import java.util.List;

public interface TelegramMessagingService {

    TelegramUpdateDTO getUpdates();

    Boolean reply(TelegramUpdateDTO telegramUpdateDTO);

    SendMessageResponseDTO sendHeartBeatMessage(Long chatId);

    void sendNewNotifications(List<Home> homeList);

    boolean sendMessage(TelegramSendMessage message);

}
