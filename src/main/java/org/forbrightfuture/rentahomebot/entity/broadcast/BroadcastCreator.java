package org.forbrightfuture.rentahomebot.entity.broadcast;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="broadcast_creator")
@Data
public class BroadcastCreator {

    @Id
    private long chatId;

}
