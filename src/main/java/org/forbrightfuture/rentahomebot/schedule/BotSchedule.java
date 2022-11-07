package org.forbrightfuture.rentahomebot.schedule;

import lombok.extern.slf4j.Slf4j;
import org.forbrightfuture.rentahomebot.constants.Website;
import org.forbrightfuture.rentahomebot.dto.HeartBeatUserDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.send.text.SendMessageDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.update.TelegramUpdateDTO;
import org.forbrightfuture.rentahomebot.entity.Home;
import org.forbrightfuture.rentahomebot.service.*;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastService;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@EnableAsync
@Slf4j
public class BotSchedule {

    private final CityService cityService;
    private final HomeService homeService;
    private final TelegramMessagingService telegramMessagingService;
    private final ChatDataService chatDataService;
    private final HeartBeatUserService heartBeatUserService;
    private final BroadcastService broadcastService;

    public BotSchedule(CityService cityService, HomeService homeService,
                       TelegramMessagingService telegramMessagingService, ChatDataService chatDataService,
                       HeartBeatUserService heartBeatUserService, BroadcastService broadcastService) {
        this.cityService = cityService;
        this.homeService = homeService;
        this.telegramMessagingService = telegramMessagingService;
        this.chatDataService = chatDataService;
        this.heartBeatUserService = heartBeatUserService;
        this.broadcastService = broadcastService;
    }

    @Scheduled(fixedRateString = "${task.update-cities.rate}")
    public void updateCities() throws IOException {
        log.info("City table update began");
        long startTime = System.currentTimeMillis();
        cityService.updateCities();
        long endTime = System.currentTimeMillis();
        log.info("City table update stopped");
        log.info("Passed time for city table update: " + (endTime - startTime) + " ms");
    }

    @Scheduled(fixedDelayString = "${task.binaaz-update-homes.rate}", initialDelay = 10000L)
    public void updateBinaazHomes() throws IOException {
        log.info("Binaaz - homes update began");
        long startTime = System.currentTimeMillis();
        homeService.findNewHomes(Website.BinaAz);
        long endTime = System.currentTimeMillis();
        log.info("Binaaz - homes update stopped");
        log.info("Binaaz - passed time for home table update: " + (endTime - startTime) + " ms");
    }

    @Scheduled(fixedDelayString = "${task.yeniemlak-update-homes.rate}", initialDelay = 20000L)
    public void updateYeniEmlakHomes() throws IOException {
        log.info("Yeniemlak - homes update began");
        long startTime = System.currentTimeMillis();
        homeService.findNewHomes(Website.YeniEmlak);
        long endTime = System.currentTimeMillis();
        log.info("Yeniemlak - homes update stopped");
        log.info("Yeniemlak - passed time for home table update: " + (endTime - startTime) + " ms");
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
        List<Home> homeList = homeService.getUnsentHomes();
        telegramMessagingService.sendNewNotifications(homeList);
    }

    @Scheduled(fixedRateString = "${task.clear-old-homes.rate}")
    public void clearOldHomesFromDatabase() {
        log.info("Clearing old homes began");
        long startTime = System.currentTimeMillis();
        homeService.clearOldHomes();
        long endTime = System.currentTimeMillis();
        log.info("Clearing old homes ended");
        log.info("Time passed for old homes deletion: " + (endTime - startTime) + " ms");
    }

    @Scheduled(fixedRateString = "${task.clear-old-messages.rate}")
    public void clearOldMessagesFromDatabase() {
        log.info("Clearing old messages began");
        chatDataService.deleteOldMessages();
        log.info("Clearing old messages ended");
    }

    @Scheduled(fixedRateString = "${task.send-heartbeat-signal.rate}")
    public void sendHeartBeatMessages() {
        List<HeartBeatUserDTO> heartBeatUserDTOS = heartBeatUserService.getAllHeartBeatUsers();
        log.info("Heart beat signals will be sent to " + heartBeatUserDTOS.size() + " users");
        for(HeartBeatUserDTO heartBeatUserDTO: heartBeatUserDTOS) {
            telegramMessagingService.sendHeartBeatMessage(heartBeatUserDTO.getChatId());
        }
        log.info("Heart beat signals were sent");
    }

    @Scheduled(fixedDelayString = "${task.send-broadcast-message.rate}")
    public void sendBroadcastMessages() {
        long startTime = System.currentTimeMillis();
        if (broadcastService.sendCustomBroadcastMessage()) {
            log.info("Broadcast message was sent!");
        }
        long endTime = System.currentTimeMillis();
        log.info("Passed time in broadcast messages: " + (endTime - startTime) + " ms");
    }

}
