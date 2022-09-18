package org.forbrightfuture.rentahomebot.service;

import org.forbrightfuture.rentahomebot.constants.Language;

public interface MessageProvider {

    String getMessage(String message, Language lang);

}
