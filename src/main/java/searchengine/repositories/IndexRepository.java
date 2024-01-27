package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<IndexEntity, Integer> {
    @Query(value = "SELECT * FROM page_index p WHERE p.id = :id", nativeQuery = true)
    List<IndexEntity> findAllByPageId(Integer id);
}
