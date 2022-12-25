package org.forbrightfuture.rentahomebot.dto.telegram.send.text;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.forbrightfuture.rentahomebot.dto.telegram.send.ReplyKeyboard;
import org.forbrightfuture.rentahomebot.dto.telegram.send.TelegramSendMessage;

@Data
public class SendTextDTO extends TelegramSendMessage {

    public SendTextDTO() {}

    public SendTextDTO(Long chatId, String text, ReplyKeyboard replyKeyboard) {
        super(chatId, "HTML");
        this.text = text;
        this.replyKeyboard = replyKeyboard;
    }

    public SendTextDTO(Long chatId, String text, String parseMode, ReplyKeyboard replyKeyboard) {
        super(chatId, parseMode);
        this.text = text;
        this.replyKeyboard = replyKeyboard;
    }

    @JsonProperty("text")
    private String text;


    @JsonProperty("reply_markup")
    private ReplyKeyboard replyKeyboard;

}
