package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import java.util.Optional;

@Repository
public interface PageRepository extends CrudRepository<PageEntity, Integer> {
    @Query(value = "SELECT * FROM page p WHERE p.path = :path", nativeQuery = true)
    Optional<PageEntity> existByPath(String path);
}
