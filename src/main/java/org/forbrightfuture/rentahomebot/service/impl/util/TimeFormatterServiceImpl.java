package org.forbrightfuture.rentahomebot.service.impl.util;

import org.forbrightfuture.rentahomebot.service.util.TimeFormatterService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service
public class TimeFormatterServiceImpl implements TimeFormatterService {

    private SimpleDateFormat sdf;

    TimeFormatterServiceImpl() {
        sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        TimeZone timeZone = TimeZone.getTimeZone("Asia/Baku");
        sdf.setTimeZone(timeZone);
    }

    @Override
    public String getDateStringInBakuTimeZone(Date date) {
        return sdf.format(date);
    }

}
