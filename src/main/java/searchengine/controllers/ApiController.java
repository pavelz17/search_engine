package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.BaseResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingSiteService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingSiteService indexingSiteService;


    public ApiController(StatisticsService statisticsService, IndexingSiteService indexingSiteService) {
        this.statisticsService = statisticsService;
        this.indexingSiteService = indexingSiteService;
    }

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
}
