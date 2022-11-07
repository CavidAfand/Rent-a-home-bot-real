package org.forbrightfuture.rentahomebot.dto.broadcast;

import lombok.Data;

@Data
public class BroadcastCreatorDTO {

    public BroadcastCreatorDTO(){}

    public BroadcastCreatorDTO(long chatId) {
        this.chatId = chatId;
    }

    private long chatId;

}
