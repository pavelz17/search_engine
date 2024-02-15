package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchInfo implements Comparable<SearchInfo> {
    String site;
    String siteName;
    String uri;
    String title;
    String snippet;
    Float relevance;

    @Override
    public int compareTo(SearchInfo searchInfo) {
        return Float.compare(searchInfo.relevance, this.relevance);
    }
}
