package org.forbrightfuture.rentahomebot.repository;

import jakarta.transaction.Transactional;
import org.forbrightfuture.rentahomebot.entity.City;
import org.forbrightfuture.rentahomebot.entity.Home;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("select h from Home h where h.alreadySent = true and " +
            " h.city = :city and " +
            "(:minAmount is null or :minAmount <= h.price) and " +
            "(:maxAmount is null or :maxAmount >= h.price) and " +
            "(:minRoomNumber is null or :minRoomNumber <= h.numberOfRoom) and " +
            "(:maxRoomNumber is null or :maxRoomNumber >= h.numberOfRoom) and" +
            "(:roomNumber is null or :roomNumber = h.numberOfRoom)")
    List<Home> getHomesByCriteria(@Param("city") City city, @Param("minAmount") Long minAmount,
                                                    @Param("maxAmount") Long maxAmount, @Param("minRoomNumber") Integer minRoomNumber,
                                                    @Param("maxRoomNumber") Integer maxRoomNumber, @Param("roomNumber") Long roomNumber);
}
