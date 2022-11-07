package org.forbrightfuture.rentahomebot.service.impl.broadcast;

import lombok.extern.slf4j.Slf4j;
import org.forbrightfuture.rentahomebot.constants.BroadcastState;
import org.forbrightfuture.rentahomebot.constants.BroadcastType;
import org.forbrightfuture.rentahomebot.dto.broadcast.BroadcastMessageDTO;
import org.forbrightfuture.rentahomebot.entity.broadcast.BroadcastMessage;
import org.forbrightfuture.rentahomebot.repository.broadcast.BroadcastMessageRepository;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastCreatorService;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastMessageService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class BroadcastMessageServiceImpl implements BroadcastMessageService {

    private final BroadcastMessageRepository broadcastMessageRepository;
    private final SimpleDateFormat dateFormatter;
    private final BroadcastCreatorService broadcastCreatorService;

    public BroadcastMessageServiceImpl(BroadcastMessageRepository broadcastMessageRepository,
                                       BroadcastCreatorService broadcastCreatorService) {
        this.broadcastMessageRepository = broadcastMessageRepository;
        this.broadcastCreatorService = broadcastCreatorService;
        this.dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }


    @Override
    public BroadcastMessageDTO getNextBroadcastMessage() {
        List<BroadcastMessage> broadcastMessages = broadcastMessageRepository
                .getCustomBroadcastMessageByNotSentOrderByBroadcastDateAsc(Pageable.ofSize(1));
        if (broadcastMessages != null && broadcastMessages.size() > 0) {
            // check broadcast date is before now
            if (broadcastMessages.get(0).getBroadcastDate().before(new Date()))
                return new BroadcastMessageDTO(broadcastMessages.get(0));
        }
        return null;
    }

    @Override
    public void setBroadcastMessageAsSent(BroadcastMessageDTO broadcastMessageDTO) {
        BroadcastMessage broadcastMessage = broadcastMessageRepository
                .getBroadcastMessageByBroadcastName(broadcastMessageDTO.getBroadcastName());
        broadcastMessage.setAlreadySent(BroadcastState.ALREADY_SENT);
        broadcastMessageRepository.save(broadcastMessage);
    }

    @Override
    public boolean saveBroadcastMessage(String text, long chatId) {
        if (broadcastCreatorService.getBroadcastCreatorById(chatId)) {
            String[] stringParts = text.split("\\|");

            BroadcastMessage broadcastMessage = new BroadcastMessage();
            broadcastMessage.setBroadcastName(stringParts[1]);
            broadcastMessage.setBroadcastContent(stringParts[2]);
            broadcastMessage.setBroadcastType(BroadcastType.CUSTOM);
            broadcastMessage.setAlreadySent(BroadcastState.NOT_SENT);
            broadcastMessage.setInsertTime(new Date());
            broadcastMessage.setSegmentId(Integer.parseInt(stringParts[3]));
            try {
                broadcastMessage.setBroadcastDate(dateFormatter.parse(stringParts[4]));
            }
            catch (Exception ex) {
                log.error("Broadcast cannot be saved in db. Exception: " + ex.getMessage() );
                return false;
            }

            broadcastMessageRepository.save(broadcastMessage);

            return true;
        }
        log.warn("Broadcast message cannot be saved because chat is not in broadcast creator list. Chat id: " + chatId);
        return false;
    }

    @Override
    public void saveBroadcastMessage(BroadcastMessageDTO broadcastMessageDTO) {
        BroadcastMessage broadcastMessage = new BroadcastMessage();
        broadcastMessage.setBroadcastName(broadcastMessageDTO.getBroadcastName());
    }
}
