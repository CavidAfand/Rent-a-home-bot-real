package org.forbrightfuture.rentahomebot.repository;

import org.forbrightfuture.rentahomebot.entity.HeartBeatUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartBeatUserRepository extends JpaRepository<HeartBeatUser, Long> {
}
