package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<IndexEntity, Integer> {


    @Transactional
    @Query(value = "SELECT * FROM page_index p WHERE p.page_id = :id", nativeQuery = true)
    List<IndexEntity> findAllByPageId(Integer id);


    @Query(value = "SELECT * FROM page_index pi WHERE pi.lemma_id = :id", nativeQuery = true)
    List<IndexEntity> findAllByLemma(Integer id);
}
