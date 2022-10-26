package org.forbrightfuture.rentahomebot.repository.broadcast;

import org.forbrightfuture.rentahomebot.entity.Chat;
import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BroadcastSegmentRepository extends JpaRepository<BroadcastSegment, Long> {

    @Query("select bs.chat from BroadcastSegment bs where bs.segmentId = ?1")
    public List<Chat> getChatIdListBySegmentId(@Param("segmentId") int segmentId);

}
