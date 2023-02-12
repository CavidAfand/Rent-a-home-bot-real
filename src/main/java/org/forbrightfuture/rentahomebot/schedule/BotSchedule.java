package org.forbrightfuture.rentahomebot.schedule;

import java.io.IOException;
import java.util.List;
import org.forbrightfuture.rentahomebot.constants.Website;
import org.forbrightfuture.rentahomebot.dto.HeartBeatUserDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.update.TelegramUpdateDTO;
import org.forbrightfuture.rentahomebot.entity.Home;
import org.forbrightfuture.rentahomebot.service.*;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastService;
import org.forbrightfuture.rentahomebot.staticVar.TimeVariables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class BotSchedule {

    private final CityService cityService;
    private final HomeService homeService;
    private final TelegramMessagingService telegramMessagingService;
    private final ChatDataService chatDataService;
    private final HeartBeatUserService heartBeatUserService;
    private final BroadcastService broadcastService;
    private final BlackHourService blackHourService;

    public BotSchedule(CityService cityService, HomeService homeService,
                       TelegramMessagingService telegramMessagingService, ChatDataService chatDataService,
                       HeartBeatUserService heartBeatUserService, BroadcastService broadcastService,
                       BlackHourService blackHourService) {
        this.cityService = cityService;
        this.homeService = homeService;
        this.telegramMessagingService = telegramMessagingService;
        this.chatDataService = chatDataService;
        this.heartBeatUserService = heartBeatUserService;
        this.broadcastService = broadcastService;
        this.blackHourService = blackHourService;
    }

    @Scheduled(fixedRateString = "${task.update-cities.rate}")
    public void updateCities() throws IOException {
        log.info("City table update began");
        TimeVariables.cityUpdateStartTime = System.currentTimeMillis();
        cityService.updateCities();
        TimeVariables.cityUpdateEndTime = System.currentTimeMillis();
        log.info("City table update stopped");
        log.info("Passed time for city table update: " +
                (TimeVariables.cityUpdateEndTime - TimeVariables.cityUpdateStartTime) + " ms");
    }

    @Scheduled(fixedDelayString = "${task.binaaz-update-homes.rate}", initialDelay = 10000L)
    public void updateBinaazHomes() throws IOException {
        if (blackHourService.isBlackHourAndNotAllowedForScrapping(Website.BinaAz)) {
            return;
        }
        log.info("Binaaz - homes update began");
        TimeVariables.binaazHomesUpdateStartTime = System.currentTimeMillis();
        homeService.findNewHomes(Website.BinaAz);
        TimeVariables.binaazHomesUpdateEndTime = System.currentTimeMillis();
        log.info("Binaaz - homes update stopped");
        log.info("Binaaz - passed time for home table update: " +
                (TimeVariables.binaazHomesUpdateEndTime - TimeVariables.binaazHomesUpdateStartTime) + " ms");
    }

    @Scheduled(fixedDelayString = "${task.yeniemlak-update-homes.rate}", initialDelay = 20000L)
    public void updateYeniEmlakHomes() throws IOException {
        if (blackHourService.isBlackHourAndNotAllowedForScrapping(Website.YeniEmlak)) {
            return;
        }
        log.info("Yeniemlak - homes update began");
        TimeVariables.yeniemlakHomesUpdateStartTime = System.currentTimeMillis();
        homeService.findNewHomes(Website.YeniEmlak);
        TimeVariables.yeniemlakHomesUpdateEndTime = System.currentTimeMillis();
        log.info("Yeniemlak - homes update stopped");
        log.info("Yeniemlak - passed time for home table update: " +
                (TimeVariables.yeniemlakHomesUpdateEndTime - TimeVariables.yeniemlakHomesUpdateStartTime) + " ms");
    }

    @Scheduled(fixedRateString = "${task.update-telegram-update.rate}")
    public void getTelegramUpdates() {
        TelegramUpdateDTO telegramUpdateDTO = telegramMessagingService.getUpdates();
        if (telegramUpdateDTO != null) {
            log.info(telegramUpdateDTO.toString());
            telegramMessagingService.reply(telegramUpdateDTO);
        }
    }

    @Scheduled(fixedDelayString = "${task.send-new-notification.rate}")
    public void sendNotificationsToUsers() {
        if (blackHourService.isBlackHour("SendNotificationToUsers schedule")) {
            return;
        }

        List<Home> homeList = homeService.getUnsentHomes();
        telegramMessagingService.sendNewNotifications(homeList);
    }

    @Scheduled(fixedRateString = "${task.clear-old-homes.rate}")
    public void clearOldHomesFromDatabase() {
        log.info("Clearing old homes began");
        TimeVariables.clearOldHomesStartTime = System.currentTimeMillis();
        homeService.clearOldHomes();
        TimeVariables.clearOldHomesEndTime = System.currentTimeMillis();
        log.info("Clearing old homes ended");
        log.info("Time passed for old homes deletion: " +
                (TimeVariables.clearOldHomesEndTime - TimeVariables.clearOldHomesStartTime) + " ms");
    }

//    @Scheduled(fixedRateString = "${task.clear-old-messages.rate}")
    public void clearOldMessagesFromDatabase() {
        log.info("Clearing old messages began");
        TimeVariables.clearOldMessagesStartTime = System.currentTimeMillis();
        chatDataService.deleteOldMessages();
        TimeVariables.clearOldMessagesEndTime = System.currentTimeMillis();
        log.info("Clearing old messages ended");
        log.info("Time passed for old messages deletion: " +
                (TimeVariables.clearOldMessagesEndTime - TimeVariables.clearOldMessagesStartTime) + " ms");
    }

    @Scheduled(fixedRateString = "${task.send-heartbeat-signal.rate}")
    public void sendHeartBeatMessages() {
        List<HeartBeatUserDTO> heartBeatUserDTOS = heartBeatUserService.getAllHeartBeatUsers();
        log.info("Heart beat signals will be sent to " + heartBeatUserDTOS.size() + " users");
        TimeVariables.heartBeatSendingStartTime = System.currentTimeMillis();
        for(HeartBeatUserDTO heartBeatUserDTO: heartBeatUserDTOS) {
            telegramMessagingService.sendHeartBeatMessage(heartBeatUserDTO.getChatId());
        }
        TimeVariables.heartBeatSendingEndTime = System.currentTimeMillis();
        log.info("Heart beat signals were sent");
        log.info("Time passed for sending heartbeat message: " +
                (TimeVariables.heartBeatSendingEndTime - TimeVariables.heartBeatSendingStartTime) + " ms");
    }

    @Scheduled(fixedDelayString = "${task.send-broadcast-message.rate}")
    public void sendBroadcastMessages() {
        TimeVariables.broadcastSendingStartTime = System.currentTimeMillis();
        if (broadcastService.sendCustomBroadcastMessage()) {
            log.info("Broadcast message was sent!");
        }
        TimeVariables.broadcastSendingEndTime = System.currentTimeMillis();
        log.info("Passed time in broadcast messages: " +
                (TimeVariables.broadcastSendingEndTime - TimeVariables.broadcastSendingStartTime) + " ms");
    }

}
