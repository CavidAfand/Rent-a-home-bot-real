package org.forbrightfuture.rentahomebot.dto.broadcast;

import lombok.Data;
import org.forbrightfuture.rentahomebot.constants.ChatStage;
import org.forbrightfuture.rentahomebot.constants.Language;
import org.forbrightfuture.rentahomebot.entity.Chat;

@Data
public class BroadcastChatDTO {

    public BroadcastChatDTO() {}

    public BroadcastChatDTO(Chat chat) {
        this.chatId = chat.getChatId();
        this.firstName = chat.getFirstName();
        this.lastName = chat.getLastName();
        this.username = chat.getUsername();
        this.chatStage = chat.getChatStage();
        this.language = chat.getLanguage();
    }

    private long chatId;
    private String firstName;
    private String lastName;
    private String username;
    private ChatStage chatStage;
    private Language language;

}
