package org.forbrightfuture.rentahomebot.dto.telegram.send.text;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.forbrightfuture.rentahomebot.dto.telegram.send.ReplyKeyboard;

@Data
public class SendMessageDTO {

    public SendMessageDTO() {}

    public SendMessageDTO(Long chatId, String text, ReplyKeyboard replyKeyboard) {
        this.chatId = chatId;
        this.text = text;
        this.replyKeyboard = replyKeyboard;
    }

    @JsonProperty("chat_id")
    private Long chatId;

    @JsonProperty("text")
    private String text;

    @JsonProperty("reply_markup")
    private ReplyKeyboard replyKeyboard;

}
