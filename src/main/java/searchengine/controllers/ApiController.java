package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.BaseResponse;
import searchengine.dto.search.SearchOptions;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingSiteService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {
    private static final String DEFAULT_OFFSET = "0";
    private static final String DEFAULT_LIMIT = "20";
    private final StatisticsService statisticsService;
    private final IndexingSiteService indexingSiteService;
    private final SearchService searchService;


    @GetMapping("/startIndexing")
    public ResponseEntity<BaseResponse> startIndexing() {
        return ResponseEntity.ok(indexingSiteService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<BaseResponse> stopIndexing() {
        return ResponseEntity.ok(indexingSiteService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<BaseResponse> indexPage(@RequestParam String url) {
        return ResponseEntity.ok(indexingSiteService.pageIndexing(url));
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse> search(@RequestParam(required = false) String query,
                                               @RequestParam(required = false) String site,
                                               @RequestParam(defaultValue = DEFAULT_OFFSET) Integer offset,
                                               @RequestParam(defaultValue = DEFAULT_LIMIT) Integer limit) {
        SearchOptions searchOptions = SearchOptions.builder()
                .query(query)
                .site(site)
                .offset(offset)
                .limit(limit)
                .build();
        return ResponseEntity.ok(searchService.getSearchResult(searchOptions));
    }
}
