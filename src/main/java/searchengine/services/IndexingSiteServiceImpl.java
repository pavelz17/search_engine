package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.dto.StartIndexingResponse;
import searchengine.dto.StopIndexingResponse;
import searchengine.exceptions.IncorrectMethodCallException;

@Service
public class IndexingSiteServiceImpl implements IndexingSiteService {
    private static final String START_INDEXING_ERROR_MESSAGE = "Индексация уже запущена";
    private static final String STOP_INDEXING_ERROR_MESSAGE = "Индексация не запущена";
    private boolean indexingRun = false;

    @Override
    public StartIndexingResponse startIndexing() {
        if(isIndexingRun()) {
            throw new IncorrectMethodCallException(START_INDEXING_ERROR_MESSAGE);
        }
        indexingRun = true;
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

}
