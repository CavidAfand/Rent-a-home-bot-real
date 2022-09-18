package org.forbrightfuture.rentahomebot.repository;

import org.forbrightfuture.rentahomebot.entity.Home;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

public interface HomeRepository extends JpaRepository<Home, Long> {

    Home getHomeByLink(String pageLink);

    @Query("select h.link from Home h")
    List<String> getPageLinks();

    @Query("select h from Home h where h.alreadySent = false")
    List<Home> getUnsentHome();

    @Transactional
    @Modifying
    @Query("delete from Home h where h.insertDate <= :date")
    void deleteOldHomes(@Param("date") Date date);

}
