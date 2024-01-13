package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.BaseResponse;
import searchengine.exceptions.IncorrectMethodCallException;
import searchengine.repositories.SiteRepository;

@Service
@RequiredArgsConstructor
public class IndexingSiteServiceImpl implements IndexingSiteService {

    private static final String START_INDEXING_ERROR_MESSAGE = "Индексация уже запущена";
    private static final String STOP_INDEXING_ERROR_MESSAGE = "Индексация не запущена";
    private boolean indexingRun = false;
    private final SitesList sites;
    private final SiteRepository siteRepository;


    @Override
    public BaseResponse startIndexing() {
        if(isIndexingRun()) {
            throw new IncorrectMethodCallException(START_INDEXING_ERROR_MESSAGE);
        }
        indexingRun = true;
        for (Site site : sites.getSites()) {
            
        }
        BaseResponse response = new BaseResponse();
        response.setResult(true);
        return response;
    }

    @Override
    public BaseResponse stopIndexing() {
        if(!isIndexingRun()) {
            throw new IncorrectMethodCallException(STOP_INDEXING_ERROR_MESSAGE);
        }

        indexingRun = false;
        BaseResponse response = new BaseResponse();
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
