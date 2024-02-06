package searchengine.mappers;

import org.springframework.stereotype.Component;
import searchengine.dto.model.LemmasDto;
import searchengine.model.LemmaEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class LemmasMapper implements Mapper<LemmasDto, List<LemmaEntity>>{

    @Override
    public List<LemmaEntity> mapFrom(LemmasDto lemmas) {
        List<LemmaEntity> lemmaEntities = new ArrayList<>();
        for (String lemma : lemmas.lemmas().keySet()) {
            LemmaEntity lemmaEntity = LemmaEntity.builder()
                    .lemma(lemma)
                    .frequency(1)
                    .build();
            lemmaEntities.add(lemmaEntity);
        }
        return lemmaEntities;
    }
}
