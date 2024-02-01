package searchengine.repositories;

import searchengine.model.LemmaEntity;

public interface CustomLemmaRepository {
    void saveLemmas(Iterable<LemmaEntity> lemmas);
}
