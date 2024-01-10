package searchengine.services;

import searchengine.dto.StartIndexingResponse;
import searchengine.dto.StopIndexingResponse;

public interface IndexingSiteService {
    StartIndexingResponse startIndexing();
    StopIndexingResponse stopIndexing();
}
