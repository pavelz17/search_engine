package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConf;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.model.LemmasDto;
import searchengine.dto.model.PageDto;
import searchengine.mappers.LemmasMapper;
import searchengine.mappers.PageMapper;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {
    private static final int LEMMAS_CAPACITY = 30;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final JsoupConf jsoupConf;
    private final SitesList sitesFromConfigFile;
    private final PageMapper pageMapper;
    private final LemmasMapper lemmasMapper;


    @Override
    public List<Site> getSitesFromConfig() {
        return sitesFromConfigFile.getSites();
    }

    @Override
    public void updateSitesInDatabaseFromConfigFile() {

        for (Site site : sitesFromConfigFile.getSites()) {
            if (this.findSiteByUrl(site.getUrl()).isEmpty()) {
                this.saveSite(site.getUrl(), site.getName());
            }
        }
    }

    @Override
    public PageEntity createPageEntity(PageDto pageDto, SiteEntity site, String url) {
        PageEntity pageEntity = pageMapper.mapFrom(pageDto);
        String path = url.replace(site.getUrl(), "/");
        pageEntity.setPath(path);
        pageEntity.setSite(site);
        return pageEntity;
    }

    @Override
    public List<LemmaEntity> createLemmaEntities(LemmasDto lemmasDto, SiteEntity site) {
        List<LemmaEntity> lemmaEntities = lemmasMapper.mapFrom(lemmasDto);
        for (LemmaEntity lemmaEntity : lemmaEntities) {
            lemmaEntity.setSite(site);
        }
        return lemmaEntities;
    }

    @Override
    public List<IndexEntity> createIndexes(PageEntity page, List<LemmaEntity> lemmaEntities, LemmasDto lemmasDto) {
        List<IndexEntity> indexes = new ArrayList<>();
        Map<String, Integer> lemmas = lemmasDto.lemmas();
        for (LemmaEntity lemma : lemmaEntities) {
            IndexEntity indexEntity = IndexEntity.builder()
                    .page(page)
                    .lemma(lemma)
                    .rate(Float.valueOf(lemmas.get(lemma.getLemma())))
                    .build();
            indexes.add(indexEntity);
        }
        return indexes;
    }

    @Override
    public SiteEntity saveSite(String url, String name) {
        String siteUrl = url;
        siteUrl = siteUrl.endsWith("/") ? siteUrl : siteUrl.concat("/");
        SiteEntity siteEntity = SiteEntity.builder()
                .status(SearchStatus.INDEXING)
                .statusTime(LocalDateTime.now())
                .name(name)
                .url(siteUrl)
                .build();
        return siteRepository.save(siteEntity);
    }

    @Override
    public PageEntity savePage(PageEntity page) {
        return pageRepository.save(page);
    }

    @Override
    public void saveLemmas(List<LemmaEntity> lemmaEntities, SiteEntity site) {
        Set<Integer> lemmasIdForUpdate = new HashSet<>(LEMMAS_CAPACITY);
        Set<LemmaEntity> lemmasForSave = new HashSet<>(LEMMAS_CAPACITY);

        for (int i = 0; i < lemmaEntities.size(); i++) {
            LemmaEntity lemmaEntity = lemmaEntities.get(i);
            Optional<LemmaEntity> maybeLemma = lemmaRepository.findByUniqueKey(lemmaEntity.getLemma(), site.getId());
            if (maybeLemma.isPresent()) {
                LemmaEntity presentLemma = maybeLemma.get();
                lemmaEntities.set(i, presentLemma);
                lemmasIdForUpdate.add(presentLemma.getId());
            } else {
                lemmasForSave.add(lemmaEntity);
            }
            if (lemmasIdForUpdate.size() >= LEMMAS_CAPACITY) {
                lemmaRepository.updateFrequencyAfterSave(lemmasIdForUpdate);
                lemmasIdForUpdate.clear();
            }
            if (lemmasForSave.size() >= LEMMAS_CAPACITY) {
                lemmaRepository.saveAll(lemmasForSave);
                lemmasForSave.clear();
            }
        }
        if (!lemmasIdForUpdate.isEmpty()) {
            lemmaRepository.updateFrequencyAfterSave(lemmasIdForUpdate);
        }
        if (!lemmasForSave.isEmpty()) {
            lemmaRepository.saveAll(lemmasForSave);
        }
    }

    @Override
    public void saveIndexes(List<IndexEntity> indexes) {
        indexRepository.saveAll(indexes);
    }

    @Override
    public void deleteSiteByUrl(String url) {
        siteRepository.deleteByUrl(url);
    }

    @Override
    public void deletePageById(Integer id) {
        deleteOrUpdateLemmasByPageId(id);
        pageRepository.deleteById(id);
    }

    @Override
    public Iterable<SiteEntity> findAllSites() {
        return siteRepository.findAll();
    }

    @Override
    public Optional<SiteEntity> findSiteByUrl(String url) {
        Pattern pattern = Pattern.compile("^https?://.+\\.(ru|com|org)/");
        Matcher matcher = pattern.matcher(url);
        String rootUrl = "";

        if (matcher.find()) {
            rootUrl = matcher.group();
        }
        return siteRepository.findByUrl(rootUrl);
    }

    @Override
    public Optional<PageEntity> findPageByUrl(SiteEntity site, String url) {
        String path = url.replace(site.getUrl(), "/");
        return pageRepository.findByPath(path);
    }


    @Override
    public void updateStatusTime(LocalDateTime localDateTime, Integer id) {
        siteRepository.updateStatusTime(localDateTime, id);
    }

    @Override
    public void updateLastError(String error, Integer id) {
        siteRepository.updateLastError(error, id);
    }

    @Override
    public void updateSearchStatus(String status, Integer id) {
        siteRepository.updateSearchStatus(status, id);
    }


    @Override
    public Connection.Response getResponse(String url) throws IOException {
        return Jsoup.connect(url).userAgent(jsoupConf.getUserAgent())
                .referrer(jsoupConf.getReferrer())
                .execute();
    }

    @Override
    public void saveAllPages(List<PageEntity> pages) {
        pageRepository.saveAll(pages);
    }

    @Override
    public int getTotalPagesCount() {
        return (int) pageRepository.count();
    }

    @Override
    public int getTotalLemmasCount() {
        return (int) lemmaRepository.count();
    }

    @Override
    public int getPagesCountBySiteId(Integer id) {
        List<PageEntity> pages = pageRepository.findAllBySiteId(id);
        return pages.size();
    }

    @Override
    public int getLemmasCountBySiteId(Integer id) {
        List<LemmaEntity> lemmas = lemmaRepository.findAllBySiteId(id);
        return lemmas.size();
    }

    private void deleteOrUpdateLemmasByPageId(Integer id) {
        List<IndexEntity> indexes = indexRepository.findAllByPageId(id);
        List<LemmaEntity> deleteLemmas = new ArrayList<>();
        List<Integer> updateLemmasIds = new ArrayList<>();
        for (IndexEntity index : indexes) {
            LemmaEntity lemma = index.getLemma();
            int frequency = lemma.getFrequency();
            if (frequency > 1) {
                updateLemmasIds.add(lemma.getId());
            } else {
                deleteLemmas.add(lemma);
            }
        }
        if (!deleteLemmas.isEmpty()) {
            lemmaRepository.deleteAll(deleteLemmas);
        }
        if (!updateLemmasIds.isEmpty()) {
            lemmaRepository.updateFrequencyAfterDelete(updateLemmasIds);
        }
    }
}
