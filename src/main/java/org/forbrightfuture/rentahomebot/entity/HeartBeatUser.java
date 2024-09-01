package org.forbrightfuture.rentahomebot.entity;

import lombok.Data;
import jakarta.persistence.*;


@Entity
@Table(name = "HEART_BEAT_USER")
@Data
public class HeartBeatUser {

    @Id
    @Column(name = "CHAT_ID")
    private Long chatId;

}
