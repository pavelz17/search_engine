package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<LemmaEntity, Integer> {

    @Modifying
    @Transactional
    public void deleteAllLemmasByIds(List<LemmaEntity> lemmas);

    @Modifying
    @Transactional
    public void updateFrequencyAllLemmasByIds(List<LemmaEntity> lemmas);
}
