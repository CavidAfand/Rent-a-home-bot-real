package org.forbrightfuture.rentahomebot.dto.telegram.send;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class TelegramSendMessage {

    @JsonProperty("chat_id")
    private Long chatId;

    @JsonProperty("parse_mode")
    private String parseMode;

}
