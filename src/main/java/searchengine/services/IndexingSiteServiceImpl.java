package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.StartIndexingResponse;
import searchengine.dto.StopIndexingResponse;
import searchengine.exceptions.IncorrectMethodCallException;
import searchengine.repositories.SiteRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingSiteServiceImpl implements IndexingSiteService {

    private static final String START_INDEXING_ERROR_MESSAGE = "Индексация уже запущена";
    private static final String STOP_INDEXING_ERROR_MESSAGE = "Индексация не запущена";
    private boolean indexingRun = false;
    private final SitesList sites;
    private final SiteRepository siteRepository;


    @Override
    public StartIndexingResponse startIndexing() {
        if(isIndexingRun()) {
            throw new IncorrectMethodCallException(START_INDEXING_ERROR_MESSAGE);
        }
        indexingRun = true;
        for (Site site : sites.getSites()) {
            
        }
        StartIndexingResponse response = new StartIndexingResponse();
        response.setResult(true);
        return response;
    }

    @Override
    public StopIndexingResponse stopIndexing() {
        if(!isIndexingRun()) {
            throw new IncorrectMethodCallException(STOP_INDEXING_ERROR_MESSAGE);
        }

        indexingRun = false;
        StopIndexingResponse response = new StopIndexingResponse();
        response.setResult(true);
        return response;
    }

    private boolean isIndexingRun() {
        return indexingRun;
    }

    private void deleteSiteDataFromDataBase(String url) {
        siteRepository.deleteByUrl(url);
    }

}
