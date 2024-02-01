package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends CrudRepository<PageEntity, Integer> {
    @Query(value = "SELECT * FROM page p WHERE p.path = :path", nativeQuery = true)
    Optional<PageEntity> findByPath(String path);

    @Query(countQuery = "SELECT count(*) from page WHERE site_id = :id", nativeQuery = true)
    List<PageEntity> findAllBySiteId(Integer id);
}
