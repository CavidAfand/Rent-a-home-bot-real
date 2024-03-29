package org.forbrightfuture.rentahomebot.service.impl;

import org.forbrightfuture.rentahomebot.dto.HomeDTO;
import org.forbrightfuture.rentahomebot.entity.City;
import org.forbrightfuture.rentahomebot.entity.Home;
import org.forbrightfuture.rentahomebot.service.DTOToModelConvService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DTOToModelConvServiceImpl implements DTOToModelConvService {

    @Override
    public Home getHome(HomeDTO homeDTO, List<City> cityList) {
        Home home = new Home();
        home.setLink(homeDTO.getPageLink());
        home.setImageLink(homeDTO.getImageLink());
        home.setPrice(homeDTO.getPrice());
        home.setPlace(homeDTO.getPlace());
        for (City city: cityList) {
            if (city.getDescription().equals(homeDTO.getCity())) {
                home.setCity(city);
                break;
            }
        }
        home.setCategory(homeDTO.getCategory());
        home.setStage(homeDTO.getStage());
        home.setNumberOfRoom(homeDTO.getNumberOfRoom());
        home.setArea(homeDTO.getArea());
        home.setTags(homeDTO.getTags());
        home.setInfo(homeDTO.getInfo());
        return home;
    }

}
