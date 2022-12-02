package org.forbrightfuture.rentahomebot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.forbrightfuture.rentahomebot.constants.ChatStage;
import org.forbrightfuture.rentahomebot.constants.Language;
import org.forbrightfuture.rentahomebot.dto.telegram.send.*;
import org.forbrightfuture.rentahomebot.dto.telegram.send.photo.SendPhotoDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.send.text.SendMessageDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.update.TelegramResponseDTO;
import org.forbrightfuture.rentahomebot.dto.telegram.update.TelegramUpdateDTO;
import org.forbrightfuture.rentahomebot.entity.Chat;
import org.forbrightfuture.rentahomebot.entity.City;
import org.forbrightfuture.rentahomebot.entity.Home;
import org.forbrightfuture.rentahomebot.entity.SearchParameter;
import org.forbrightfuture.rentahomebot.service.*;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastMessageService;
import org.forbrightfuture.rentahomebot.service.broadcast.BroadcastService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    private final HttpRequestService httpRequestService;
    private final ChatDataService chatDataService;
    private final MessageProvider messageProvider;
    private final CityService cityService;
    private final SearchParameterService searchParameterService;
    private final HomeService homeService;
    private final BroadcastMessageService broadcastMessageService;

    private Long offset = null;

    public TelegramMessagingServiceImpl(HttpRequestService httpRequestService, ChatDataService chatDataService,
                                        MessageProvider messageProvider, CityService cityService,
                                        SearchParameterService searchParameterService, HomeService homeService,
                                        BroadcastMessageService broadcastMessageService) {
        this.httpRequestService = httpRequestService;
        this.chatDataService = chatDataService;
        this.messageProvider = messageProvider;
        this.cityService = cityService;
        this.searchParameterService = searchParameterService;
        this.homeService = homeService;
        this.broadcastMessageService = broadcastMessageService;
    }

    @Override
    public TelegramUpdateDTO getUpdates() {
        String url = telegramApiBaseUrl + "/bot" + botToken + "/getUpdates";
        if (offset != null)
            url = url + "?offset=" + offset;
        TelegramResponseDTO telegramResponseDTO = httpRequestService.sendGetRequest(url, TelegramResponseDTO.class);
        if (telegramResponseDTO.getResult().size() > 0) {
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
    public SendMessageResponseDTO sendMessage(SendMessageDTO sendMessageDTO) {
        waitForSendingMessage();
        String url = telegramApiBaseUrl + "/bot" + botToken + "/sendMessage";
        SendMessageResponseDTO responseDTO = httpRequestService.sendPostRequest(url, sendMessageDTO, SendMessageResponseDTO.class);
        return responseDTO;
    }

    @Override
    public SendMessageResponseDTO sendPhoto(SendPhotoDTO sendPhotoDTO) {
        waitForSendingMessage();
        String url = telegramApiBaseUrl + "/bot" + botToken + "/sendPhoto";
        SendMessageResponseDTO responseDTO = httpRequestService.sendPostRequest(url, sendPhotoDTO, SendMessageResponseDTO.class);
        return responseDTO;
    }

    @Override
    public void sendNewNotifications(List<Home> homeList) {
        for (Home home: homeList) {
            List<Chat> chatList = searchParameterService.getChatListByAppropriateParameters(home);
            for (Chat chat: chatList) {
                try {
                    log.info(new ObjectMapper().writeValueAsString(getNewHomeNotification(home, chat.getChatId(), chat.getLanguage())));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                SendMessageResponseDTO sendMessageResponseDTO = sendPhoto(getNewHomeNotification(home, chat.getChatId(), chat.getLanguage()));
                if (sendMessageResponseDTO.getOk() == null || sendMessageResponseDTO.getOk() == false) {
                    if (sendMessageResponseDTO.getParameters() != null && sendMessageResponseDTO.getParameters().getMigratedChatId() != null) {
                        Chat checkChat = chatDataService.getChatByChatId(sendMessageResponseDTO.getParameters().getMigratedChatId());
                        if (checkChat == null) {
                            chat.setChatId(sendMessageResponseDTO.getParameters().getMigratedChatId());
                            chat.setType("supergroup");
                            chatDataService.updateChat(chat);
                            sendPhoto(getNewHomeNotification(home, chat.getChatId(), chat.getLanguage()));
                        } else {
                            chat.setChatStage(ChatStage.START);
                            chatDataService.updateChat(chat);
                        }
                    } else if (sendMessageResponseDTO.getOk() == false && sendMessageResponseDTO.getDescription().equals("Forbidden: bot was blocked by the user")) {
                        chat.setChatStage(ChatStage.BOT_BLOCKED);
                        chatDataService.updateChat(chat);
                        searchParameterService.deleteSearchParameter(chat.getChatId());
                    }
                }
            }
            home.setAlreadySent(true);
            homeService.updateHome(home);
        }
    }

    @Override
    public SendMessageResponseDTO reply(TelegramUpdateDTO telegramUpdateDTO) {

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
                || text.equals("/reset") || text.equals("/about") || text.startsWith("/broadcast")) {

            if (text.equals("/reset")) {
                chat.setChatStage(ChatStage.START);
                chat = chatDataService.updateChat(chat);
                searchParameterService.deleteSearchParameter(chatId);
                sendMessage(getResetInfoMessage(chatId, chat.getLanguage()));
            }
            else if (text.equals("/about")) {
                return sendMessage(getAuthorInfoMessage(chatId, chat.getLanguage()));
            }
            else if (text.startsWith("/broadcast")) {
                if (broadcastMessageService.saveBroadcastMessage(text, chatId))
                    return sendMessage(getBroadcastSavedMessage(chatId));
            }

            if (!(text.equals("azərbaycanca") || text.equals("русский") || text.equals("english"))) {
                return sendMessage(getLanguageChoiceMessage(chatId));
            }
            else {
                if (text.equals("azərbaycanca")) chat.setLanguage(Language.az);
                else if (text.equals("english")) chat.setLanguage(Language.en);
                else chat.setLanguage(Language.ru);
                chat.setChatStage(ChatStage.CITY);
                chat = chatDataService.updateChat(chat);
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
                chat.setChatStage(ChatStage.PRICE_LOW);
                chatDataService.updateChat(chat);
                return sendMessage(getPriceQuestionMessage(chatId, chat.getLanguage(), true));
            }
            else {
                return sendMessage(getCityChoiceMessage(chatId, chat.getLanguage()));
            }
        }
        // price
        else if (chat.getChatStage() == ChatStage.PRICE_LOW || chat.getChatStage() == ChatStage.PRICE_HIGH) {
            // check if this parameter was skipped
            if (text.equals(messageProvider.getMessage("skip_button", chat.getLanguage()))) {
                // do nothing
            } else {
                Long enteredPrice = null;
                try {
                    enteredPrice = Long.parseLong(text);
                } catch (NumberFormatException ex) {
                    log.error("Incorrect price. Entered value: " + enteredPrice);
                    sendMessage(getInvalidNumberErrorMessage(chatId, chat.getLanguage()));
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
                chat.setChatStage(ChatStage.PRICE_HIGH);
                chatDataService.updateChat(chat);
                return sendMessage(getPriceQuestionMessage(chatId, chat.getLanguage(), false));
            }
            else {
                chat.setChatStage(ChatStage.ROOM_NUMBER);
                chatDataService.updateChat(chat);
                return sendMessage(getRoomNumberQuestionMessage(chatId, chat.getLanguage()));
            }
        }
        // room number
        else if (chat.getChatStage() == ChatStage.ROOM_NUMBER) {
            SearchParameter searchParameter = null;
            if (text.equals(messageProvider.getMessage("skip_button", chat.getLanguage()))) {
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
                    sendMessage(getInvalidNumberErrorMessage(chatId, chat.getLanguage()));
                    return sendMessage(getRoomNumberQuestionMessage(chatId, chat.getLanguage()));
                }


                searchParameter = searchParameterService.getSearchParameter(chatId);
                searchParameter.setNumberOfRoom(enteredNumber);
                searchParameter.setMinNumberOfRoom(minRoomNumber);
                searchParameter.setMaxNumberOfRoom(maxRoomNumber);
                searchParameter = searchParameterService.updateSearchParameter(searchParameter);
            }

            chat.setChatStage(ChatStage.READY_RECEIVED);
            chat = chatDataService.updateChat(chat);

            if (searchParameter == null)
                searchParameter = searchParameterService.getSearchParameter(chatId);

            // finish message
            sendMessage(getSearchParametersFinishMessage(chatId, chat.getLanguage(), searchParameter));
            sendMessage(getReadyInfoMessage(chatId, chat.getLanguage()));
            return sendMessage(getFraudWarningMessage(chatId, chat.getLanguage()));
        }

        return null;
    }

    @Override
    public SendMessageResponseDTO sendHeartBeatMessage(Long chatId) {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setChatId(chatId);
        sendMessageDTO.setParseMode("HTML");
        sendMessageDTO.setText("Heart beat signal. No worries. Bot is working");
        sendMessageDTO.setReplyKeyboard(new ReplyKeyboardRemoveDTO(false));
        return sendMessage(sendMessageDTO);
    }

    private SendMessageDTO getBroadcastSavedMessage(long chatId) {
        return new SendMessageDTO(chatId, "Broadcast message was saved", new ReplyKeyboardRemoveDTO(true));
    }

    private SendMessageDTO getLanguageChoiceMessage(Long chatId) {
        // prepare keyboard
        KeyboardButtonDTO [][] buttons = new KeyboardButtonDTO[2][];
        buttons[0] = new KeyboardButtonDTO[1];
        buttons[1] = new KeyboardButtonDTO[2];
        buttons[0][0] = new KeyboardButtonDTO(messageProvider.getMessage("language_az", null));
        buttons[1][0] = new KeyboardButtonDTO(messageProvider.getMessage("language_en", null));
        buttons[1][1]  = new KeyboardButtonDTO(messageProvider.getMessage("language_ru", null));
        ReplyKeyboardMarkupDTO replyKeyboardMarkupDTO = new ReplyKeyboardMarkupDTO();
        replyKeyboardMarkupDTO.setKeyboardButtonArray(buttons);
        replyKeyboardMarkupDTO.setOneTimeKeyboard(true);

        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setChatId(chatId);
        sendMessageDTO.setParseMode("HTML");
        sendMessageDTO.setText(messageProvider.getMessage("start_message", null));
        sendMessageDTO.setReplyKeyboard(replyKeyboardMarkupDTO);

        return sendMessageDTO;
    }

    private SendMessageDTO getCityChoiceMessage(Long chatId, Language language) {
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
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setChatId(chatId);
        sendMessageDTO.setParseMode("HTML");
        sendMessageDTO.setText(messageProvider.getMessage("question_city_choice", language));
        sendMessageDTO.setReplyKeyboard(replyKeyboardMarkupDTO);

        return sendMessageDTO;
    }

    private SendMessageDTO getPriceQuestionMessage(Long chatId, Language language, boolean isLowPriceQuestion) {
        SendMessageDTO sendMessageDTO = getSkipableQuestion(language);
        sendMessageDTO.setChatId(chatId);
        sendMessageDTO.setParseMode("HTML");
        if (isLowPriceQuestion == true)
            sendMessageDTO.setText(messageProvider.getMessage("question_min_price", language));
        else
            sendMessageDTO.setText(messageProvider.getMessage("question_max_price", language));
        return sendMessageDTO;
    }

    private SendMessageDTO getRoomNumberQuestionMessage(Long chatId, Language language) {
        SendMessageDTO sendMessageDTO = getSkipableQuestion(language);
        sendMessageDTO.setChatId(chatId);
        sendMessageDTO.setParseMode("HTML");
        sendMessageDTO.setText(messageProvider.getMessage("question_room_number", language));
        return sendMessageDTO;
    }

    private SendMessageDTO getSkipableQuestion(Language language) {
        KeyboardButtonDTO [][] buttons = new KeyboardButtonDTO[1][1];
        buttons[0][0] = new KeyboardButtonDTO(messageProvider.getMessage("skip_button", language));
        ReplyKeyboardMarkupDTO replyKeyboard = new ReplyKeyboardMarkupDTO();
        replyKeyboard.setOneTimeKeyboard(true);
        replyKeyboard.setKeyboardButtonArray(buttons);

        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setReplyKeyboard(replyKeyboard);
        sendMessageDTO.setParseMode("HTML");
        return sendMessageDTO;
    }

    private SendMessageDTO getReadyInfoMessage(Long chatId, Language language) {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setChatId(chatId);
        sendMessageDTO.setText(messageProvider.getMessage("ready_info", language));
        sendMessageDTO.setParseMode("HTML");
        sendMessageDTO.setReplyKeyboard(new ReplyKeyboardRemoveDTO(true));
        return sendMessageDTO;
    }

    private SendMessageDTO getFraudWarningMessage(Long chatId, Language language) {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setChatId(chatId);
        sendMessageDTO.setText(messageProvider.getMessage("fraud_warning", language));
        sendMessageDTO.setParseMode("HTML");
        sendMessageDTO.setReplyKeyboard(new ReplyKeyboardRemoveDTO(true));
        return sendMessageDTO;
    }

    private SendMessageDTO getInvalidNumberErrorMessage(Long chatId, Language language) {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setChatId(chatId);
        sendMessageDTO.setText(messageProvider.getMessage("invalid_number_info", language));
        sendMessageDTO.setParseMode("HTML");
        sendMessageDTO.setReplyKeyboard(new ReplyKeyboardRemoveDTO(true));
        return sendMessageDTO;
    }

    private SendMessageDTO getResetInfoMessage(Long chatId, Language language) {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setText(messageProvider.getMessage("reset_info", language));
        sendMessageDTO.setChatId(chatId);
        sendMessageDTO.setParseMode("HTML");
        sendMessageDTO.setReplyKeyboard(new ReplyKeyboardRemoveDTO(true));
        return sendMessageDTO;
    }

    private SendMessageDTO getAuthorInfoMessage(Long chatId, Language language) {
        String text = messageProvider.getMessage("author", language) + ": Javid Afandiyev\nhttps://www.linkedin.com/in/javid-afandiyev-63825014b/";

        SendMessageDTO sendMessageDTO = new SendMessageDTO(chatId, text, new ReplyKeyboardRemoveDTO(true));

        return sendMessageDTO;
    }

    private SendMessageDTO getSearchParametersFinishMessage(Long chatId, Language language, SearchParameter searchParameter) {
        String city = searchParameter.getCity().getDescription();
        String minPrice = (searchParameter.getMinPrice() != null)? searchParameter.getMinPrice().toString(): messageProvider.getMessage("notification.no_entered", language);
        String maxPrice = (searchParameter.getMaxPrice() != null)? searchParameter.getMaxPrice().toString(): messageProvider.getMessage("notification.no_entered", language);
//        String numberOfRoom = (searchParameter.getNumberOfRoom() != null)? searchParameter.getNumberOfRoom().toString(): messageProvider.getMessage("notification.no_entered", language);
        String numberOfRoom = null;
        if (searchParameter.getNumberOfRoom() != null) {
            numberOfRoom = searchParameter.getNumberOfRoom().toString();
        }
        else if (searchParameter.getMinNumberOfRoom() != null) {
            numberOfRoom = searchParameter.getMinNumberOfRoom().toString() + "-" + searchParameter.getMaxNumberOfRoom();
        }
        else {
            numberOfRoom = messageProvider.getMessage("notification.no_entered", language);
        }
        String text = messageProvider.getMessage("entered_search_params", language) + ": \n" +
                    "- " + messageProvider.getMessage("notification.city", language) + ": " + city + "\n" +
                "- " + messageProvider.getMessage("notification.min_price", language) + ": " + minPrice + "\n" +
                "- " + messageProvider.getMessage("notification.max_price", language) + ": " + maxPrice + "\n" +
                "- " + messageProvider.getMessage("notification.number_of_room", language) + ": " + numberOfRoom;

        SendMessageDTO sendMessageDTO = new SendMessageDTO(chatId, text, new ReplyKeyboardRemoveDTO(true));
        return sendMessageDTO;
    }

    private SendPhotoDTO getNewHomeNotification(Home home, Long chatId, Language language) {
        String info = (home.getInfo().length() <= 300)? home.getInfo(): home.getInfo().substring(0, 300) + "....";
        String stage = home.getStage() != null?home.getStage():"-";
        String notificationText = "<b>" + messageProvider.getMessage("notification.address", language) + ":</b> " + home.getCity().getDescription() + " - " + home.getPlace() + "\n" +
                "<b>" + messageProvider.getMessage("notification.price", language) + ":</b> " + home.getPrice() + "\n" +
                "<b>" + messageProvider.getMessage("notification.home_category", language) + ":</b> " + home.getCategory() + "\n" +
                "<b>" + messageProvider.getMessage("notification.room_number", language) + ":</b> " + home.getNumberOfRoom() + "\n" +
                "<b>" + messageProvider.getMessage("notification.floor", language) + ":</b> " + stage + "\n" +
                "<b>" + messageProvider.getMessage("notification.area", language) + ":</b> " + home.getArea() + "\n" +
                "<b>" + messageProvider.getMessage("notification.info", language) + ":</b> " + info + "\n\n" +
                "<b>" + messageProvider.getMessage("notification.link.title", language) + ":</b> " + "<a href=\"" + home.getLink() + "\">" + messageProvider.getMessage("notification.link.text", language) + "</a>";

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
