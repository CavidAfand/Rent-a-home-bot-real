package org.forbrightfuture.rentahomebot.service.broadcast;

import java.util.List;

public interface BroadcastService {

    public void sendBroadcast(String message, List<String> chatIdList);

}
