package org.forbrightfuture.rentahomebot.entity.broadcast;


import lombok.Data;
import org.forbrightfuture.rentahomebot.entity.Chat;
import jakarta.persistence.*;
import java.util.List;


@Data
@Table(name = "broadcast_segment")
@Entity
public class BroadcastSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "segment_id", nullable = false)
    private int segmentId;

    @ManyToMany
    @Column(name = "chat_id")
    private List<Chat> chat;

}
