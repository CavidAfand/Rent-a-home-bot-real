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

@Service("yeniemlak")
@Slf4j
public class YeniEmlakScrapServiceImpl implements ScrapService {

    @Value("${yeniemlak.homepage.url}")
    private String homepageUrl;

    @Override
    public List<String> getCities() throws IOException {
        // not implemented
        return null;
    }

    @Override
    public List<HomeDTO> getHomes(List<String> homeLinks) throws IOException {
        long startTime = System.currentTimeMillis();

        List<HomeDTO> apartmentHomes = getApartmentHomes(homeLinks);
        List<HomeDTO> newHomes = new ArrayList<>();
        apartmentHomes.stream().forEach(home -> newHomes.add(home));

        log.info("Yeniemlak get new homes time: " + (System.currentTimeMillis() - startTime) + " ms");
        return newHomes;
    }

    private List<HomeDTO> getApartmentHomes(List<String> homeLinks) throws IOException {
        String rentPage = homepageUrl + "/elan/axtar?emlak=1&elan_nov=2";
        List<HomeDTO> homes = getNewHomes(rentPage, homeLinks);
        log.info("Yeniemlak new apartment home list size: " + homes.size());
        return homes;
    }

    private List<HomeDTO> getNewHomes(String rentPage, List<String> homeLinks) throws IOException {
        List<HomeDTO> homes = new ArrayList<>();
        Document rentPageDocument = Jsoup.connect(rentPage).get();
        Elements announcementPanels = rentPageDocument.getElementsByClass("list");
        for (Element announcement: announcementPanels) {
            String pageLink = homepageUrl + announcement.selectFirst("a.detail").attr("href");
            if (homeLinks.contains(pageLink))
                continue;
            HomeDTO homeDTO = new HomeDTO();
            homeDTO.setPageLink(pageLink);
            homeDTO.setPrice(Long.parseLong(announcement.selectFirst("price").text()));
            homeDTO.setCategory(announcement.selectFirst("td.text").text().contains("Yeni tikili")?"Yeni tikili":"Köhnə tikili");
            Elements parameters = announcement.getElementsByClass("params");
            int paramIndex = 0;
            for (Element parameter: parameters) {
                if (paramIndex == 0) {
                    homeDTO.setNumberOfRoom(Long.parseLong(parameter.selectFirst("b").text()));
                }
                else if (paramIndex == 1) {
                    homeDTO.setArea(parameter.selectFirst("b").text() + " m²");
                }
                else if (paramIndex == 2) {
                    homeDTO.setStage(parameter.text().replace(" Mərtəbə", ""));
                }
                else if (paramIndex == 3) {
                    String city = parameter.selectFirst("b").text().trim();
                    if (city.indexOf(" ") != -1)
                        homeDTO.setCity(city.substring(0, city.indexOf(" ")));
                    else
                        homeDTO.setCity(city);
                }
                else if (paramIndex == 4) {
                    homeDTO.setPlace(parameter.selectFirst("b").text());
                }
                else {
                    if (homeDTO.getTags() == null) {
                        homeDTO.setTags(parameter.selectFirst("b").text());
                    }
                    else {
                        homeDTO.setTags(homeDTO.getTags() + ", " + parameter.text());
                    }
                }

                paramIndex++;
            }

            Elements dataElements = announcement.select("tr");
            Element tdElement = dataElements.get(1).selectFirst("td.text");

            String infoSide = tdElement.html().split("<br>")[2];

            String [] infoData = infoSide.substring(0, infoSide.indexOf("<div>")).split("<hr>");

            homeDTO.setInfo(infoData[0].replace("\n","").trim()  + " - " + infoData[1].trim());

            // taking home image
            Document homePage = Jsoup.connect(homeDTO.getPageLink()).get();
            homeDTO.setImageLink(homepageUrl + homePage.selectFirst("img.imgb").attr("src"));

            if (homeDTO.getImageLink().equals("https://yeniemlak.az/img/nophoto.jpg"))
                continue;

            // delay because of website bot checking
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            homes.add(homeDTO);
        }

        return homes;
    }


}
