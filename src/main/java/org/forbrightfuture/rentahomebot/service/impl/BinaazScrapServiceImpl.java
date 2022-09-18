package org.forbrightfuture.rentahomebot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.forbrightfuture.rentahomebot.dto.HomeDTO;
import org.forbrightfuture.rentahomebot.service.ScrapService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("binaaz")
@Slf4j
public class BinaazScrapServiceImpl implements ScrapService {

    @Value("${binaaz.homepage.url}")
    private String homepageUrl;

    @Override
    public List<String> getCities() throws IOException {
        Document homePage = Jsoup.connect(homepageUrl).get();
        Elements cityLiElements = homePage.select("select.city option");
        List<String> cities = new ArrayList<>();
        for (Element cityLiElement: cityLiElements) {
            if (!cityLiElement.text().equals("İstənilən şəhər"))
                cities.add(cityLiElement.text());
        }
        return cities;
    }

    @Override
    public List<HomeDTO> getHomes(List<String> homeLinks) throws IOException {
        long startTime = System.currentTimeMillis();

        List<HomeDTO> apartmentHomes = getApartmentHomes(homeLinks);
        List<HomeDTO> simpleHomes = getSimpleHomes(homeLinks);
        List<HomeDTO> newHomes = new ArrayList<>();
        apartmentHomes.stream().forEach(home -> newHomes.add(home));
        simpleHomes.stream().forEach(home -> newHomes.add(home));

        log.info("Binaaz get new homes time: " + (System.currentTimeMillis() - startTime) + " ms");
        return newHomes;
    }

    private List<HomeDTO> getApartmentHomes(List<String> homeLinks) throws IOException {
        String rentPage = homepageUrl + "/kiraye/menziller";
        List<HomeDTO> homes = getNewHomes(rentPage, homeLinks);
        log.info("Binaaz new apartment home list size: " + homes.size());
        return homes;
    }

    private List<HomeDTO> getSimpleHomes(List<String> homeLinks) throws IOException {
        String rentPage = homepageUrl + "/kiraye/evler";
        List<HomeDTO> homes = getNewHomes(rentPage, homeLinks);
        log.info("Binaaz new simple home list size: " + homes.size());
        return homes;
    }

    private List<HomeDTO> getNewHomes(String rentPage, List<String> homeLinks) throws IOException {
        List<HomeDTO> homes = new ArrayList<>();
        Document rentPageDocument = Jsoup.connect(rentPage).get();
        Elements announcementPanels = rentPageDocument.getElementsByClass("items_list");
        for (Element announcementPanel: announcementPanels) {
            Elements announcementCards = announcementPanel.getElementsByClass("items-i");
            for (Element announcementCard: announcementCards) {
                String paymentPeriod = announcementCard.selectFirst("span.price-per").text();
                if (paymentPeriod.toLowerCase().contains("ay")) {
                    String pageLink = homepageUrl + announcementCard.select("a").attr("href");
                    if (!homeLinks.contains(pageLink)) {
                        HomeDTO homeDTO = new HomeDTO();
                        homeDTO.setPageLink(pageLink);
                        homeDTO.setImageLink(announcementCard.selectFirst("img").attr("data-src"));
                        Element announcementCardParams = announcementCard.selectFirst("div.card_params");
                        homeDTO.setPrice(
                                Long.parseLong(announcementCardParams.selectFirst("span.price-val").text()
                                        .replaceAll(" ", ""))
                        );
                        homeDTO.setPlace(announcementCardParams.selectFirst("div.location").text());
                        homeDTO.setCity(announcementCardParams.selectFirst("div.city_when").text().split(",")[0]);
                        homeDTO = getHomeDetails(homeDTO);
                        homes.add(homeDTO);
                    }
                }
            }
        }
        return homes;
    }

    private HomeDTO getHomeDetails(HomeDTO homeDTO) throws IOException {
        Document homeDetailsPageDocument = Jsoup.connect(homeDTO.getPageLink()).get();
        Elements details = homeDetailsPageDocument.select("table.parameters > tbody > tr");
        for (Element detail: details) {
            Elements detailParams = detail.select("td");
            String paramTitle = detailParams.get(0).text();
            String paramValue = detailParams.get(1).text();
            switch (paramTitle) {
                case "Kateqoriya":
                    homeDTO.setCategory(paramValue);
                    break;
                case "Mərtəbə":
                    homeDTO.setStage(paramValue);
                    break;
                case "Sahə":
                    homeDTO.setArea(paramValue);
                    break;
                case "Otaq sayı":
                    homeDTO.setNumberOfRoom(Long.parseLong(paramValue)  );
                    break;
            }
        }

        Elements badges = homeDetailsPageDocument.select("div.badges_blokc ul.locations li");
        List<String> tags = new ArrayList<>();
        for (Element badge: badges) {
            tags.add(badge.selectFirst("a").text());
        }
        homeDTO.setTags(tags.stream().collect(Collectors.joining(",")));
        homeDTO.setInfo(homeDetailsPageDocument.select("article").text());
        return homeDTO;
    }

}
