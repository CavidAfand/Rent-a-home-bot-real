package org.forbrightfuture.rentahomebot.entity.broadcast;

import lombok.Data;
import org.forbrightfuture.rentahomebot.constants.BroadcastState;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "broadcast_table")
@Entity
public class BroadcastMessage {

    @Id
    @Column(name = "broadcast_name", length = 120, nullable = false)
    private String broadcastName;

    @Lob
    @Column(name = "broadcast_content", nullable = false)
    private String broadcastContent;

    @Column(name = "already_sent", nullable = false)
    private BroadcastState alreadySent;

    @Column(name = "insert_time", nullable = false)
    private Date insertTime;

    @Column(name = "segment_id", nullable = false)
    private int segmentId;

}
