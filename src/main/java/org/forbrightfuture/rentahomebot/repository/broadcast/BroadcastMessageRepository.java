package org.forbrightfuture.rentahomebot.repository.broadcast;

import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, Long> {

    @Query("select bm from BroadcastMessage bm " +
            "where bm.alreadySent = org.forbrightfuture.rentahomebot.constants.BroadcastState.NOT_SENT " +
            "order by bm.id asc")
    public List<BroadcastMessage> getBroadcastMessageByFirstNotSent();

}
