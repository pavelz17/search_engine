package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LemmaRepository extends CrudRepository<LemmaEntity, Integer>, CustomLemmaRepository {

    @Modifying
    @Transactional
    @Query(value = "UPDATE lemma SET frequency = frequency + 1 WHERE id IN (:ids)", nativeQuery = true)
    void updateFrequencyAfterSave(Set<Integer> ids);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lemma SET frequency = frequency - 1 WHERE id IN (:ids)", nativeQuery = true)
    void updateFrequencyAfterDelete(List<Integer> ids);

<<<<<<< HEAD
    @Transactional
=======
>>>>>>> 3dcef69a06a1b24917eef2146edf44e632b5fc10
    @Query(value = "SELECT * FROM lemma l WHERE l.lemma = :lemma AND l.site_id = :siteId", nativeQuery = true)
    Optional<LemmaEntity> findByUniqueKey(String lemma, Integer siteId);

    @Transactional
<<<<<<< HEAD
=======
    @Query(value = "UPDATE lemma SET frequency = frequency + 1 WHERE id = :id", nativeQuery = true)
    void update(Integer id);

>>>>>>> 3dcef69a06a1b24917eef2146edf44e632b5fc10
    @Query(countQuery = "SELECT count(*) from lemma WHERE site_id = :id", nativeQuery = true)
    List<LemmaEntity> findAllBySiteId(Integer id);
}
