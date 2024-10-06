package org.forbrightfuture.rentahomebot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.forbrightfuture.rentahomebot.constants.ChatStage;
import org.forbrightfuture.rentahomebot.constants.Language;
import org.forbrightfuture.rentahomebot.service.util.TimeFormatterService;
import org.forbrightfuture.rentahomebot.staticVar.MessageNames;
import org.forbrightfuture.rentahomebot.dto.telegram.send.*;
import org.forbrightfuture.rentahomebot.dto.telegram.send.photo.SendPhotoDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.send.text.SendTextDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.update.TelegramResponseDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.update.TelegramUpdateDTO;
import org.forbrightfuture.rentahomebot.entity.Chat;
import org.forbrightfuture.rentahomebot.entity.City;
import org.forbrightfuture.rentahomebot.entity.Home;
import org.forbrightfuture.rentahomebot.entity.SearchParameter;
import org.forbrightfuture.rentahomebot.service.*;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TelegramMessagingServiceImpl implements TelegramMessagingService {

    @Value("${telegram.api.base-url}")
    private String telegramApiBaseUrl;

    @Value("${telegram.api.token}")
    private String botToken;

    @Value("${telegram.bot.name}")
    private String botName;

    @Value("${coldstart-nofication-count}")
    private int coldStartNotificationCount;

    private final HttpRequestService httpRequestService;
    private final ChatDataService chatDataService;
    private final MessageProvider messageProvider;
    private final CityService cityService;
    private final SearchParameterService searchParameterService;
    private final HomeService homeService;
    private final BroadcastMessageService broadcastMessageService;
    private final TimeFormatterService timeFormatterService;

    private Long offset = null;

    public TelegramMessagingServiceImpl(HttpRequestService httpRequestService, ChatDataService chatDataService,
                                        MessageProvider messageProvider, CityService cityService,
                                        SearchParameterService searchParameterService, HomeService homeService,
                                        BroadcastMessageService broadcastMessageService, TimeFormatterService timeFormatterService) {
        this.httpRequestService = httpRequestService;
        this.chatDataService = chatDataService;
        this.messageProvider = messageProvider;
        this.cityService = cityService;
        this.searchParameterService = searchParameterService;
        this.homeService = homeService;
        this.broadcastMessageService = broadcastMessageService;
        this.timeFormatterService = timeFormatterService;
    }

    @Override
    public TelegramUpdateDTO getUpdates() {
        String url = telegramApiBaseUrl + "/bot" + botToken + "/getUpdates";
        if (offset != null)
            url = url + "?offset=" + offset;
        TelegramResponseDTO telegramResponseDTO = httpRequestService.sendGetRequest(url, TelegramResponseDTO.class);
        if (!telegramResponseDTO.getResult().isEmpty()) {
            if (telegramResponseDTO.getResult().get(0).getMessageDTO() != null) {
                TelegramUpdateDTO telegramUpdateDTO = telegramResponseDTO.getResult().get(0);
                log.info(telegramUpdateDTO.toString());
                telegramUpdateDTO.getMessageDTO().setDate(telegramUpdateDTO.getMessageDTO().getDate() * 1000);
                chatDataService.saveTelegramMessage(telegramUpdateDTO);
                offset = telegramUpdateDTO.getUpdateId() + 1;
                return telegramUpdateDTO;
            }
            else {
                offset = telegramResponseDTO.getResult().get(0).getUpdateId() + 1;
                return null;
            }
        }
        else
            return null;
    }

    @Override
    public void sendNewNotifications(List<Home> homeList) {
        for (Home home: homeList) {
            List<Chat> chatList = searchParameterService.getChatListByAppropriateParameters(home);
            for (Chat chat: chatList) {
                try {
                    log.info(new ObjectMapper().writeValueAsString(getNewHomeNotification(home, chat.getChatId(), chat.getLanguage(), false)));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                sendMessage(getNewHomeNotification(home, chat.getChatId(), chat.getLanguage(), false));
            }
            home.setAlreadySent(true);
            homeService.updateHome(home);
        }
    }

    @Override
    public boolean sendMessage(TelegramSendMessage message) {
        SendMessageResponseDTO sendMessageResponseDTO = null;

        if (message instanceof SendPhotoDTO) {
            sendMessageResponseDTO = sendPhoto((SendPhotoDTO) message);
        }
        else if (message instanceof SendTextDTO) {
            sendMessageResponseDTO = sendText((SendTextDTO) message);
        }
        else {
            log.error("message is unknown. Type of message is " + message.getClass().getSimpleName());
        }

        long chatId = message.getChatId();

        if (sendMessageResponseDTO.getOk() == null || sendMessageResponseDTO.getOk() == false) {
            if (sendMessageResponseDTO.getParameters() != null && sendMessageResponseDTO.getParameters().getMigratedChatId() != null) {
                Chat checkChat = chatDataService.getChatByChatId(sendMessageResponseDTO.getParameters().getMigratedChatId());
                if (checkChat == null) {
                    checkChat = chatDataService.migrateChatId(chatId, sendMessageResponseDTO.getParameters().getMigratedChatId());
                    message.setChatId(checkChat.getChatId());
                    sendMessage(message);
                }
            } else if (!sendMessageResponseDTO.getOk() && sendMessageResponseDTO.getDescription().equals("Forbidden: bot was blocked by the user")) {
                chatDataService.updateChatStage(chatId, ChatStage.BOT_BLOCKED);
                searchParameterService.deleteSearchParameter(chatId);
            } else if (!sendMessageResponseDTO.getOk() && sendMessageResponseDTO.getDescription().equals("Forbidden: user is deactivated")) {
                chatDataService.updateChatStage(chatId, ChatStage.USER_DEACTIVED);
                searchParameterService.deleteSearchParameter(chatId);
            }
        }

        return sendMessageResponseDTO.getOk() != null ? sendMessageResponseDTO.getOk() : false;
    }

    @Override
    public Boolean reply(TelegramUpdateDTO telegramUpdateDTO) {

        // check it is private or group chat
        if (telegramUpdateDTO.getMessageDTO().getChat().getType().equals("group")) {
            String callName = "@" + botName;
            if (telegramUpdateDTO.getMessageDTO().getText().startsWith(callName)) {
                telegramUpdateDTO.getMessageDTO().setText(telegramUpdateDTO.getMessageDTO().getText().substring(callName.length()).trim());
                System.out.println("RESULT: " + telegramUpdateDTO.getMessageDTO().getText());
            }
            else if (telegramUpdateDTO.getMessageDTO().getReplyToMessage() != null) {
                if (!telegramUpdateDTO.getMessageDTO().getReplyToMessage().getFrom().getUsername().equals(botName))
                    return null;
            }
            else
                return null;
        }

        Long chatId = telegramUpdateDTO.getMessageDTO().getChat().getId();
        String text = telegramUpdateDTO.getMessageDTO().getText().trim();
        Chat chat = chatDataService.getChatByChatId(chatId);

        if (chat.getChatStage() == ChatStage.START || chat.getChatStage() == ChatStage.BOT_BLOCKED
                || text.equals("/reset") || text.equals("/author") || text.startsWith("/broadcast")) {

            if (text.equals("/reset")) {
                chatDataService.updateChatStage(chatId, ChatStage.START);
                searchParameterService.deleteSearchParameter(chatId);
                sendMessage(getRawTextMessage(chatId, MessageNames.ResetInfo, chat.getLanguage(), new ReplyKeyboardRemoveDTO(true)));
            }
            else if (text.equals("/author")) {
                return sendMessage(getAuthorInfoMessage(chatId, chat.getLanguage()));
            }
            else if (text.startsWith("/broadcast")) {
                if (broadcastMessageService.saveBroadcastMessage(text, chatId))
                    return sendMessage(getRawTextMessage(chatId, MessageNames.BroadcastSaved, chat.getLanguage(), new ReplyKeyboardRemoveDTO(true)));
            }

            if (!(text.equals("azərbaycanca") || text.equals("русский") || text.equals("english"))) {
                return sendMessage(getLanguageChoiceMessage(chatId));
            }
            else {
                if (text.equals("azərbaycanca")) chat.setLanguage(Language.az);
                else if (text.equals("english")) chat.setLanguage(Language.en);
                else chat.setLanguage(Language.ru);
                chat.setChatStage(ChatStage.CITY);
                chatDataService.updateChat(chat);
                return sendMessage(getCityChoiceMessage(chatId, chat.getLanguage()));
            }
        }
        // city select
        else if (chat.getChatStage() == ChatStage.CITY) {
            City city = cityService.getCityByCityName(text);
            if (city != null) {
                SearchParameter searchParameter = new SearchParameter();
                searchParameter.setChat(chat);
                searchParameter.setCity(city);
                searchParameterService.saveSearchParameter(searchParameter);
                chatDataService.updateChatStage(chatId, ChatStage.PRICE_LOW);
                return sendMessage(getPriceQuestionMessage(chatId, chat.getLanguage(), true));
            }
            else {
                return sendMessage(getCityChoiceMessage(chatId, chat.getLanguage()));
            }
        }
        // price
        else if (chat.getChatStage() == ChatStage.PRICE_LOW || chat.getChatStage() == ChatStage.PRICE_HIGH) {
            // check if this parameter was skipped
            if (text.equals(messageProvider.getMessage(MessageNames.SkipButton, chat.getLanguage()))) {
                // do nothing
            } else {
                Long enteredPrice = null;
                try {
                    enteredPrice = Long.parseLong(text);
                } catch (NumberFormatException ex) {
                    log.error("Incorrect price. Entered value: " + enteredPrice);
                    sendMessage(getRawTextMessage(chatId, MessageNames.InvalidNumberInfo, chat.getLanguage(), new ReplyKeyboardRemoveDTO(true)));
                    return sendMessage(getPriceQuestionMessage(chatId, chat.getLanguage(), chat.getChatStage() == ChatStage.PRICE_LOW));
                }
                SearchParameter searchParameter = searchParameterService.getSearchParameter(chatId);
                if (chat.getChatStage() == ChatStage.PRICE_LOW) {
                    searchParameter.setMinPrice(enteredPrice);
                }
                else {
                    searchParameter.setMaxPrice(enteredPrice);
                }

                searchParameterService.updateSearchParameter(searchParameter);
            }

            if (chat.getChatStage() == ChatStage.PRICE_LOW) {
                chatDataService.updateChatStage(chatId, ChatStage.PRICE_HIGH);
                return sendMessage(getPriceQuestionMessage(chatId, chat.getLanguage(), false));
            }
            else {
                chatDataService.updateChatStage(chatId, ChatStage.ROOM_NUMBER);
                return sendMessage(getRoomNumberQuestionMessage(chatId, chat.getLanguage()));
            }
        }
        // room number
        else if (chat.getChatStage() == ChatStage.ROOM_NUMBER) {
            SearchParameter searchParameter = null;
            if (text.equals(messageProvider.getMessage(MessageNames.SkipButton, chat.getLanguage()))) {
                // do nothing
            } else {
                Long enteredNumber = null;
                Integer minRoomNumber = null;
                Integer maxRoomNumber = null;
                try {
                    if (text.contains("-")) {
                        text = text.replaceAll("\\s+","");
                        String [] numbers = text.split("-");
                        minRoomNumber = Integer.parseInt(numbers[0]);
                        maxRoomNumber = Integer.parseInt(numbers[1]);
                    }
                    else {
                        enteredNumber = Long.parseLong(text);
                    }
                }
                catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    log.error("Incorrect value. Entered value: " + text);
                    sendMessage(getRawTextMessage(chatId, MessageNames.InvalidNumberInfo, chat.getLanguage(), new ReplyKeyboardRemoveDTO(true)));
                    return sendMessage(getRoomNumberQuestionMessage(chatId, chat.getLanguage()));
                }


                searchParameter = searchParameterService.getSearchParameter(chatId);
                searchParameter.setNumberOfRoom(enteredNumber);
                searchParameter.setMinNumberOfRoom(minRoomNumber);
                searchParameter.setMaxNumberOfRoom(maxRoomNumber);
                searchParameter = searchParameterService.updateSearchParameter(searchParameter);
            }

            chatDataService.updateChatStage(chatId, ChatStage.READY_RECEIVED);

            if (searchParameter == null)
                searchParameter = searchParameterService.getSearchParameter(chatId);

            // finish message
            sendMessage(getSearchParametersFinishMessage(chatId, chat.getLanguage(), searchParameter));
            sendMessage(getRawTextMessage(chatId, MessageNames.ReadyInfo, chat.getLanguage(), new ReplyKeyboardRemoveDTO(true)));
            sendMessage(getRawTextMessage(chatId, MessageNames.FraudWarning, chat.getLanguage(), new ReplyKeyboardRemoveDTO(true)));
            return sendMessage(sendColdStart(chatId, chat.getLanguage()));
        }

        return null;
    }

    @Override
    public SendMessageResponseDTO sendHeartBeatMessage(Long chatId) {
        return sendText(getRawTextMessage(chatId, MessageNames.HeartBeatSignal, null, (new ReplyKeyboardRemoveDTO(false))));
    }

    private SendMessageResponseDTO sendText(SendTextDTO sendTextDTO) {
        waitForSendingMessage();
        String url = telegramApiBaseUrl + "/bot" + botToken + "/sendMessage";
        SendMessageResponseDTO responseDTO = httpRequestService.sendPostRequest(url, sendTextDTO, SendMessageResponseDTO.class);
        return responseDTO;
    }

    private SendMessageResponseDTO sendPhoto(SendPhotoDTO sendPhotoDTO) {
        waitForSendingMessage();
        String url = telegramApiBaseUrl + "/bot" + botToken + "/sendPhoto";
        SendMessageResponseDTO responseDTO = httpRequestService.sendPostRequest(url, sendPhotoDTO, SendMessageResponseDTO.class);
        return responseDTO;
    }

    private SendTextDTO sendColdStart(long chatId, Language language) {
        SearchParameter searchParameter = searchParameterService.getSearchParameter(chatId);
        List<Home> homes = homeService.getHomesByCriteria(searchParameter, coldStartNotificationCount);

        if (homes.isEmpty()) {
            return getRawTextMessage(chatId, MessageNames.NotificationWait, language, new ReplyKeyboardRemoveDTO(true));
        }

        for (Home home: homes) {
            sendMessage(getNewHomeNotification(home, chatId, language, true));
        }

        return getRawTextMessage(chatId, MessageNames.coldStartMessage, language, new ReplyKeyboardRemoveDTO(true));
    }

    private SendTextDTO getLanguageChoiceMessage(Long chatId) {
        // prepare keyboard
        KeyboardButtonDTO [][] buttons = new KeyboardButtonDTO[2][];
        buttons[0] = new KeyboardButtonDTO[1];
        buttons[1] = new KeyboardButtonDTO[2];
        buttons[0][0] = new KeyboardButtonDTO(messageProvider.getMessage(MessageNames.LanguageAz, null));
        buttons[1][0] = new KeyboardButtonDTO(messageProvider.getMessage(MessageNames.LanguageEn, null));
        buttons[1][1]  = new KeyboardButtonDTO(messageProvider.getMessage(MessageNames.LanguageRu, null));
        ReplyKeyboardMarkupDTO replyKeyboardMarkupDTO = new ReplyKeyboardMarkupDTO();
        replyKeyboardMarkupDTO.setKeyboardButtonArray(buttons);
        replyKeyboardMarkupDTO.setOneTimeKeyboard(true);

        return getRawTextMessage(chatId, MessageNames.StartMessage, null, replyKeyboardMarkupDTO);
    }

    private SendTextDTO getCityChoiceMessage(Long chatId, Language language) {
        int columnSize = 3;
        List<City> cityList = cityService.getCityList();
        int rowCount = (cityList.size()%columnSize==0) ? cityList.size()/columnSize : cityList.size()/columnSize+1;

        KeyboardButtonDTO[][] buttons = new KeyboardButtonDTO[rowCount][];
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            int columnCount = columnSize;
            if (rowIndex == rowCount-1 && cityList.size()%columnSize != 0) {
                columnCount = cityList.size()%columnSize;
            }
            buttons[rowIndex] = new KeyboardButtonDTO[columnCount];

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                buttons[rowIndex][columnIndex] = new KeyboardButtonDTO(cityList.get(rowIndex*columnSize + columnIndex).getDescription());
            }
        }

        ReplyKeyboardMarkupDTO replyKeyboardMarkupDTO = new ReplyKeyboardMarkupDTO();
        replyKeyboardMarkupDTO.setKeyboardButtonArray(buttons);
        replyKeyboardMarkupDTO.setOneTimeKeyboard(true);

        return getRawTextMessage(chatId, MessageNames.QuestionCityChoice, language, replyKeyboardMarkupDTO);
    }

    private SendTextDTO getPriceQuestionMessage(Long chatId, Language language, boolean isLowPriceQuestion) {
        ReplyKeyboard skipButtonKeyboard = getSkipButtonKeyboard(language);
        if (isLowPriceQuestion)
            return getRawTextMessage(chatId, MessageNames.QuestionMinPrice, language, skipButtonKeyboard);
        else
            return getRawTextMessage(chatId, MessageNames.QuestionMaxPrice, language, skipButtonKeyboard);
    }

    private SendTextDTO getRoomNumberQuestionMessage(Long chatId, Language language) {
        ReplyKeyboard skipButtonKeyboard = getSkipButtonKeyboard(language);
        return getRawTextMessage(chatId, MessageNames.QuestionRoomNumber, language, skipButtonKeyboard);
    }

    private ReplyKeyboard getSkipButtonKeyboard(Language language) {
        KeyboardButtonDTO [][] buttons = new KeyboardButtonDTO[1][1];
        buttons[0][0] = new KeyboardButtonDTO(messageProvider.getMessage(MessageNames.SkipButton, language));
        ReplyKeyboardMarkupDTO replyKeyboard = new ReplyKeyboardMarkupDTO();
        replyKeyboard.setOneTimeKeyboard(true);
        replyKeyboard.setKeyboardButtonArray(buttons);
        return replyKeyboard;
    }

    private SendTextDTO getRawTextMessage(Long chatId, String messageName, Language language, ReplyKeyboard replyKeyboard) {
        // TODO change replyKeyboard
        SendTextDTO sendTextDTO = new SendTextDTO();
        sendTextDTO.setChatId(chatId);
        sendTextDTO.setText(messageProvider.getMessage(messageName, language));
        sendTextDTO.setParseMode("HTML");
        sendTextDTO.setReplyKeyboard(new ReplyKeyboardRemoveDTO(true));
        return sendTextDTO;
    }

    private SendTextDTO getAuthorInfoMessage(Long chatId, Language language) {
        String text = messageProvider.getMessage(MessageNames.Author, language) + ": Javid Afandiyev\nhttps://www.linkedin.com/in/javid-afandiyev-63825014b/";

        SendTextDTO sendTextDTO = new SendTextDTO(chatId, text, new ReplyKeyboardRemoveDTO(true));

        return sendTextDTO;
    }

    private SendTextDTO getSearchParametersFinishMessage(Long chatId, Language language, SearchParameter searchParameter) {
        String city = searchParameter.getCity().getDescription();
        String minPrice = (searchParameter.getMinPrice() != null)? searchParameter.getMinPrice().toString(): messageProvider.getMessage(MessageNames.ParameterNoEntered, language);
        String maxPrice = (searchParameter.getMaxPrice() != null)? searchParameter.getMaxPrice().toString(): messageProvider.getMessage(MessageNames.ParameterNoEntered, language);
        String numberOfRoom = null;
        if (searchParameter.getNumberOfRoom() != null) {
            numberOfRoom = searchParameter.getNumberOfRoom().toString();
        }
        else if (searchParameter.getMinNumberOfRoom() != null) {
            numberOfRoom = searchParameter.getMinNumberOfRoom().toString() + "-" + searchParameter.getMaxNumberOfRoom();
        }
        else {
            numberOfRoom = messageProvider.getMessage(MessageNames.ParameterNoEntered, language);
        }
        String text = messageProvider.getMessage(MessageNames.EnteredSearchParameters, language) + ": \n" +
                    "- " + messageProvider.getMessage(MessageNames.ParameterCity, language) + ": " + city + "\n" +
                "- " + messageProvider.getMessage(MessageNames.ParameterMinPrice, language) + ": " + minPrice + "\n" +
                "- " + messageProvider.getMessage(MessageNames.ParameterMaxPrice, language) + ": " + maxPrice + "\n" +
                "- " + messageProvider.getMessage(MessageNames.ParameterNumberOfRoom, language) + ": " + numberOfRoom;

        return new SendTextDTO(chatId, text, new ReplyKeyboardRemoveDTO(true));
    }

    private SendPhotoDTO getNewHomeNotification(Home home, Long chatId, Language language, boolean isTimeNeeded) {
        String info = (home.getInfo().length() <= 300) ? home.getInfo() : home.getInfo().substring(0, 300) + "....";
        String stage = home.getStage() != null ? home.getStage() : "-";
        String notificationText = "<b>" + messageProvider.getMessage(MessageNames.NotificationAddress, language) + ":</b> " + home.getCity().getDescription() + " - " + home.getPlace() + "\n" +
                "<b>" + messageProvider.getMessage(MessageNames.NotificationPrice, language) + ":</b> " + home.getPrice() + "\n" +
                "<b>" + messageProvider.getMessage(MessageNames.NotificationHomeCategory, language) + ":</b> " + home.getCategory() + "\n" +
                "<b>" + messageProvider.getMessage(MessageNames.NotificationRoomNumber, language) + ":</b> " + home.getNumberOfRoom() + "\n" +
                "<b>" + messageProvider.getMessage(MessageNames.NotificationFloor, language) + ":</b> " + stage + "\n" +
                "<b>" + messageProvider.getMessage(MessageNames.NotificationArea, language) + ":</b> " + home.getArea() + "\n" +
                "<b>" + messageProvider.getMessage(MessageNames.NotificationInfo, language) + ":</b> " + info + "\n\n" +
                "<b>" + messageProvider.getMessage(MessageNames.NotificationLinkTitle, language) + ":</b> " + "<a href=\"" + home.getLink() + "\">" + messageProvider.getMessage(MessageNames.NotificationLinkText, language) + "</a>";

        if (isTimeNeeded) {
            notificationText += "\n\n" +
                    "<b>" + messageProvider.getMessage(MessageNames.homeInsertTime, language) + ":</b> " + (timeFormatterService.getDateStringInBakuTimeZone(home.getInsertDate()));
        }

        SendPhotoDTO sendPhotoDTO = new SendPhotoDTO();
        sendPhotoDTO.setChatId(chatId);
        sendPhotoDTO.setPhoto(home.getImageLink());
        sendPhotoDTO.setParseMode("HTML");
        sendPhotoDTO.setCaption(notificationText);

        return sendPhotoDTO;
    }

    // This method was used becuase Telegram is able to handle 30 messages per second
    private void waitForSendingMessage() {
        try {
            Thread.sleep(90L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
