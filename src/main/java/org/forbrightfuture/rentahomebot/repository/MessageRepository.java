package org.forbrightfuture.rentahomebot.repository;

import org.forbrightfuture.rentahomebot.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Transactional
    @Modifying
    @Query("delete from Message m where m.date <= :date")
    void deleteOldMessages(@Param("date") Date date);

}
