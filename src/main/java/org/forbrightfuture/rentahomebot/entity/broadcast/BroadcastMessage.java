package org.forbrightfuture.rentahomebot.entity.broadcast;

import lombok.Data;
import org.forbrightfuture.rentahomebot.constants.BroadcastState;
import org.forbrightfuture.rentahomebot.constants.BroadcastType;
import jakarta.persistence.*;
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

    @Column(name = "broadcast_date", nullable = true)
    private Date broadcastDate;

    @Column(name = "broadcast_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BroadcastType broadcastType;

    @Column(name = "insert_time", nullable = false)
    private Date insertTime;

    @Column(name = "segment_id", nullable = false)
    private int segmentId;

}
