package org.forbrightfuture.rentahomebot.dto.telegram.send.photo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.forbrightfuture.rentahomebot.dto.telegram.send.TelegramSendMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SendPhotoDTO extends TelegramSendMessage {

    @JsonProperty("photo")
    private String photo;

    @JsonProperty("caption")
    private String caption;

}
