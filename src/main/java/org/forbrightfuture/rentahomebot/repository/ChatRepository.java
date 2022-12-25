package org.forbrightfuture.rentahomebot.repository;

import org.forbrightfuture.rentahomebot.constants.ChatStage;
import org.forbrightfuture.rentahomebot.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Chat getChatByChatId(Long chatId);

    @Modifying
    @Query("update Chat set chatStage = :chatStage where chatId = :chatId")
    void updateChatStage(@Param("chatId") Long chatId, @Param("chatStage") ChatStage chatStage);

}
