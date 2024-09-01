package org.forbrightfuture.rentahomebot.entity.broadcast;

import lombok.Data;
import jakarta.persistence.*;


@Entity
@Table(name="broadcast_creator")
@Data
public class BroadcastCreator {

    @Id
    private long chatId;

}
