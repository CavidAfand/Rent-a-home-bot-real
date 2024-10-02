package org.forbrightfuture.rentahomebot.service;

import org.forbrightfuture.rentahomebot.constants.Website;
import org.forbrightfuture.rentahomebot.entity.Home;
import org.forbrightfuture.rentahomebot.entity.SearchParameter;

import java.io.IOException;
import java.util.List;

public interface HomeService {

    void findNewHomes(Website website) throws IOException;

    List<Home> getUnsentHomes();

    Home updateHome(Home home);

    void clearOldHomes();

    List<Home> getHomesByCriteria(SearchParameter searchParameter, int count);

}
