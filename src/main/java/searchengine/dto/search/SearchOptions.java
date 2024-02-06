package searchengine.dto.search;

import lombok.Builder;

@Builder
public record SearchOptions(String query,
                            String site,
                            Integer offset,
                            Integer limit) {
}
