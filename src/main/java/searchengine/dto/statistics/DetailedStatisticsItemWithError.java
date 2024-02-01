package searchengine.dto.statistics;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DetailedStatisticsItemWithError extends DetailedStatisticsItem {
    private String error;
}

