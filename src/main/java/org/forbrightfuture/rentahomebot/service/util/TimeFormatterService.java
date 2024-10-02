package org.forbrightfuture.rentahomebot.service.util;

import java.util.Date;

public interface TimeFormatterService {
    String getDateStringInBakuTimeZone(Date date);
}
