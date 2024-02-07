package searchengine.dto.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import searchengine.dto.BaseResponse;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchResponse extends BaseResponse {
    int count;
    List<SearchInfo> data = new ArrayList<>();
}
