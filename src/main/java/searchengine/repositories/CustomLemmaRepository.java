package searchengine.repositories;

import searchengine.model.LemmaEntity;

import java.util.List;
import java.util.Set;

public interface CustomLemmaRepository {
    Set<LemmaEntity> findAllByLemmaAndSiteId(List<LemmaEntity> lemmas);
}
