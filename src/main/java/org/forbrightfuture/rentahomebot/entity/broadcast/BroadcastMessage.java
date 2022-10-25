package org.forbrightfuture.rentahomebot.entity.broadcast;

import lombok.Data;
import org.forbrightfuture.rentahomebot.constants.BroadcastState;

import javax.persistence.*;

@Data
@Table(name = "broadcast_table")
@Entity
public class BroadcastMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "broadcast_name", length = 120, nullable = false)
    private String broadcastName;

    @Lob
    @Column(name = "broadcast_content", nullable = false)
    private String broadcastContent;

    @Column(name = "already_sent", nullable = false)
    private BroadcastState alreadySent;

}
