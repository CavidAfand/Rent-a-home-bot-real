package org.forbrightfuture.rentahomebot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.forbrightfuture.rentahomebot.constants.Website;
import org.forbrightfuture.rentahomebot.dto.HomeDTO;
import org.forbrightfuture.rentahomebot.entity.City;
import org.forbrightfuture.rentahomebot.entity.Home;
import org.forbrightfuture.rentahomebot.entity.SearchParameter;
import org.forbrightfuture.rentahomebot.repository.HomeRepository;
import org.forbrightfuture.rentahomebot.service.CityService;
import org.forbrightfuture.rentahomebot.service.DTOToModelConvService;
import org.forbrightfuture.rentahomebot.service.HomeService;
import org.forbrightfuture.rentahomebot.service.ScrapService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class HomeServiceImpl implements HomeService {

    private final HomeRepository homeRepository;
    private final ScrapService binaazScrapService;
    private final ScrapService yeniEmlakScrapService;
    private final DTOToModelConvService dtoToModelConvService;
    private final CityService cityService;

    @Value("${home.delete.time}")
    private long daysInMs;

    public HomeServiceImpl(HomeRepository homeRepository, @Qualifier("binaaz") ScrapService binaazScrapService,
                           @Qualifier("yeniemlak") ScrapService yeniEmlakScrapService,
                           DTOToModelConvService dtoToModelConvService, CityService cityService) {
        this.homeRepository = homeRepository;
        this.binaazScrapService = binaazScrapService;
        this.yeniEmlakScrapService = yeniEmlakScrapService;
        this.dtoToModelConvService = dtoToModelConvService;
        this.cityService = cityService;
    }


    @Override
    public void findNewHomes(Website website) throws IOException {
        List<String> pageLinks = homeRepository.getPageLinks();

        List<HomeDTO> homeDTOList = new ArrayList<>();
        if (website == Website.BinaAz)
            homeDTOList = binaazScrapService.getHomes(pageLinks);
        else if (website == Website.YeniEmlak)
            homeDTOList = yeniEmlakScrapService.getHomes(pageLinks);

        if (homeDTOList.size() > 0) {
            List<City> cityList = cityService.getCityList();
            homeDTOList.stream().forEach(item -> {
                    Home home = dtoToModelConvService.getHome(item, cityList);
                    home.setAlreadySent(false);
                    homeRepository.save(home);
            });
        }
    }

    @Override
    public List<Home> getUnsentHomes() {
        return homeRepository.getUnsentHome();
    }

    @Override
    public Home updateHome(Home home) {
        return homeRepository.save(home);
    }

    @Override
    public void clearOldHomes() {
        Date date = new Date(System.currentTimeMillis() - daysInMs);
        log.info("Date for old homes: " + date);
        homeRepository.deleteOldHomes(date);
    }

    @Override
    public List<Home> getHomesByCriteria(SearchParameter searchParameter, int count) {
        List<Home> homes = homeRepository.getHomesByCriteria(searchParameter.getCity(), searchParameter.getMinPrice(),
                searchParameter.getMaxPrice(), searchParameter.getMinNumberOfRoom(),
                searchParameter.getMaxNumberOfRoom(), searchParameter.getNumberOfRoom());

        return homes.stream()
                .sorted(Comparator.comparing(Home::getInsertDate).reversed())
                .limit(count)
                .toList();
    }

}
