package org.forbrightfuture.rentahomebot.repository.broadcast;

import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, String> {

    @Query("select bm from BroadcastMessage bm " +
            "where bm.alreadySent = org.forbrightfuture.rentahomebot.constants.BroadcastState.NOT_SENT " +
            "and  bm.broadcastType = org.forbrightfuture.rentahomebot.constants.BroadcastType.CUSTOM " +
            "order by bm.broadcastDate asc")
    List<BroadcastMessage> getCustomBroadcastMessageByNotSentOrderByBroadcastDateAsc(Pageable pageable);

    BroadcastMessage getBroadcastMessageByBroadcastName(String broadcastName);
}
