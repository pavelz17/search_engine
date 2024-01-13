package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import searchengine.model.SiteEntity;

public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {
    @Query(nativeQuery = true)
    void deleteByUrl(String url);
}
