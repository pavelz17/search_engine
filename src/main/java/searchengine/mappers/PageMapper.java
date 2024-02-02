package searchengine.mappers;

import org.springframework.stereotype.Component;
import searchengine.dto.model.PageDto;
import searchengine.model.PageEntity;

@Component
public class PageMapper implements Mapper<PageDto, PageEntity> {

    @Override
    public PageEntity mapFrom(PageDto pageDto) {
        return  PageEntity.builder()
                .code(pageDto.code())
                .content(pageDto.content())
                .build();
    }
}
