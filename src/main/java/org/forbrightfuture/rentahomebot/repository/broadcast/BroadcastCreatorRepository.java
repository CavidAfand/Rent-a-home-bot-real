package org.forbrightfuture.rentahomebot.repository.broadcast;

import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastCreator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BroadcastCreatorRepository extends JpaRepository<BroadcastCreator, Long> {
}
