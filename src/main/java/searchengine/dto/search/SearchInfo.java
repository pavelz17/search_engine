package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchInfo {
    String site;
    String siteName;
    String uri;
    String title;
    String snippet;
    Float relevance;
}
