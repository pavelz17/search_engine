package searchengine.repositories;

import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomLemmaRepositoryImpl implements CustomLemmaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public Set<LemmaEntity> findAllByLemma(List<LemmaEntity> lemmaEntities) {
        String prefix = "select l from LemmaEntity l where l.lemma in (";
        String suffix = " and l.site.id = :siteId";
        Set<LemmaEntity> presentLemmas = new HashSet<>();

        Map<Integer, List<String>> groupLemmasBySiteId = lemmaEntities.stream()
                .collect(Collectors.groupingBy(lemma -> lemma.getSite().getId(),
                         Collectors.mapping(LemmaEntity::getLemma, Collectors.toList())));

        for (Map.Entry<Integer, List<String>> entry : groupLemmasBySiteId.entrySet()) {
            List<String> lemmas = entry.getValue();
            StringBuilder query = new StringBuilder();
            query.append(prefix);
            for (String lemma : lemmas) {
                 query.append("'")
                        .append(lemma)
                        .append("'")
                        .append(",");
            }
            query.replace(query.length() - 1, query.length(), ")");
            query.append(suffix);
            List<LemmaEntity> resultList = entityManager.createQuery(query.toString(), LemmaEntity.class)
                                                        .setParameter("siteId", entry.getKey())
                                                        .getResultList();

            presentLemmas.addAll(resultList);
        }
        return presentLemmas;
    }
}
