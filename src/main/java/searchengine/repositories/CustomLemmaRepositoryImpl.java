package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class CustomLemmaRepositoryImpl implements CustomLemmaRepository {
    private static final String PREFIX = "INSERT INTO lemma (lemma, site_id) VALUES ";
    private static final String SUFFIX = " ON DUPLICATE KEY UPDATE frequency = frequency + 1";

    @PersistenceContext
    private EntityManager entityManager;


    @Modifying
    @Transactional
    @Override
    public void saveLemmas(Iterable<LemmaEntity> lemmas) {
        ArrayList<String> values = new ArrayList<>();

        for (LemmaEntity lemma : lemmas) {
            StringBuilder builder = new StringBuilder();
            builder.append("(")
                    .append("'")
                    .append(lemma.getLemma())
                    .append("'")
                    .append(",")
                    .append(lemma.getSite().getId())
                    .append(")");
            values.add(builder.toString());
        }

        String query = values.stream()
                .collect(Collectors.joining(",", PREFIX, SUFFIX));

        entityManager.createNativeQuery(query).executeUpdate();
    }
}
