package org.forbrightfuture.rentahomebot.dto.broadcast;

import lombok.Data;
import org.forbrightfuture.rentahomebot.constants.BroadcastState;
import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastMessage;

import java.util.Date;

@Data
public class BroadcastMessageDTO {

    public BroadcastMessageDTO() {}

    public BroadcastMessageDTO(BroadcastMessage broadcastMessage) {
        this.broadcastName = broadcastMessage.getBroadcastName();
        this.broadcastContent = broadcastMessage.getBroadcastContent();
        this.insertTime = broadcastMessage.getInsertTime();
        this.alreadySent = broadcastMessage.getAlreadySent();
        this.segmentId = broadcastMessage.getSegmentId();
    }

    private String broadcastName;
    private String broadcastContent;
    private BroadcastState alreadySent;
    private Date insertTime;
    private int segmentId;

}
