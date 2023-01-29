package org.forbrightfuture.rentahomebot.service;

import org.forbrightfuture.rentahomebot.constants.Website;

public interface BlackHourService {

    boolean isBlackHour(String element);

    boolean isBlackHourAndNotAllowedForScrapping(Website website);

}
