package searchengine.dto.statistics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import searchengine.dto.BaseResponse;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatisticsResponse extends BaseResponse {
    private StatisticsData statistics;
}
