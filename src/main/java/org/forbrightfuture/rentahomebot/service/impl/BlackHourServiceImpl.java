package org.forbrightfuture.rentahomebot.service.impl;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.forbrightfuture.rentahomebot.constants.Website;
import org.forbrightfuture.rentahomebot.service.BlackHourService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class BlackHourServiceImpl implements BlackHourService {

    private final LocalTime blackHourStart;
    private final LocalTime blackHourEnd;
    private final Map<Website, Integer> websiteMap = new HashMap<>();

    public BlackHourServiceImpl(@Value("${black-hour-start}") String blackHourStartString,
                                @Value("${black-hour-end}") String blackHourEndString) {

        String[] startTime = blackHourStartString.split(":");
        String[] endTime = blackHourEndString.split(":");

        blackHourStart = LocalTime.of(Integer.parseInt(startTime[0]), Integer.parseInt(startTime[1]), 0);
        blackHourEnd = LocalTime.of(Integer.parseInt(endTime[0]), Integer.parseInt(endTime[1]), 0);

        for(Website website: Website.values()) {
            websiteMap.put(website, 1);
        }
    }

    @Override
    public boolean isBlackHour(String element) {
        boolean result = false;
        LocalTime now = LocalTime.now();

        if ((blackHourStart.compareTo(blackHourEnd) > 0)
                && (now.compareTo(blackHourStart) > 0 || now.compareTo(blackHourEnd) < 0)) {
            result = true;
        }
        else if ((blackHourStart.compareTo(blackHourEnd) < 0)
                && (now.compareTo(blackHourStart) > 0 && now.compareTo(blackHourEnd) < 0)) {
            result = true;
        }

        if (result)
            log.info(element + " is in black hour");

        return result;
    }

    @Override
    public boolean isBlackHourAndNotAllowedForScrapping(Website website) {
        boolean result;

        if (isBlackHour(website.name())) {
            if (websiteMap.get(website) % 10 == 0) {
                log.info(website.name() + " scraping started in black hours");
                result = false;
            }
            else {
                result = true;
            }
            websiteMap.put(website, websiteMap.get(website)+1);
        }
        else {
            websiteMap.put(website, 1);
            return false;
        }

        return result;
    }

}
