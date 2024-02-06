package searchengine.mappers;

import org.springframework.stereotype.Component;
import searchengine.dto.model.PageDto;
import searchengine.model.PageEntity;

@Component
public class PageMapper implements Mapper<PageDto, PageEntity> {

    @Override
    public PageEntity mapFrom(PageDto page) {
        return  PageEntity.builder()
                .code(page.code())
                .content(page.content())
                .build();
    }
}
