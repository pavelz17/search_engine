package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {
    private static final int LEMMAS_CAPACITY = 100;
    private static final String REGEX_SITE_URL = "^https?://.+\\.(ru|com|org)/";
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
    public List<LemmaEntity> createLemmaEntities(LemmasDto lemmas, SiteEntity site) {
        List<LemmaEntity> lemmaEntities = lemmasMapper.mapFrom(lemmas);
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

    @Transactional
    @Override
    public void saveLemmas(List<LemmaEntity> lemmaEntities, SiteEntity site) {
        Set<Integer> lemmasIdForUpdate = new HashSet<>(LEMMAS_CAPACITY);
        Set<LemmaEntity> lemmasForSave = new HashSet<>(LEMMAS_CAPACITY);
        Set<LemmaEntity> presentLemmas = lemmaRepository.findAllByLemmaAndSiteId(lemmaEntities);

        if (presentLemmas.isEmpty()) {
            lemmaRepository.saveAll(lemmaEntities);
        } else {
            for (LemmaEntity lemma : lemmaEntities) {
                if (!presentLemmas.contains(lemma)) {
                    lemmasForSave.add(lemma);
                }
            }
            for (LemmaEntity lemma : presentLemmas) {
                lemmasIdForUpdate.add(lemma.getId());
            }

            lemmaRepository.saveAll(lemmasForSave);
            lemmaRepository.updateFrequencyAfterSave(lemmasIdForUpdate);

            lemmaEntities.clear();
            lemmaEntities.addAll(lemmasForSave);
            lemmaEntities.addAll(presentLemmas);
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
        Pattern pattern = Pattern.compile(REGEX_SITE_URL);
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
    public List<LemmaEntity> findLemmasBySiteId(List<String> lemmas, Integer siteId) {
        return lemmaRepository.findLemmasBySiteId(lemmas, siteId);
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
    public String getPageTitle(PageEntity page) {
        String title = "title";
        Pattern pattern = Pattern.compile("(?<=<title>).+(?=</title>)");
        Matcher matcher = pattern.matcher(page.getContent());
        if (matcher.find()) {
            title = matcher.group();
        }

        return title;
    }

    @Override
    public String getSnippet(PageEntity page, String query) {
        String[] queryWords = query.replaceAll("\\p{Punct}", " ")
                                   .split("\\s+");
        String content = Jsoup.parse(page.getContent()).text();
        StringBuilder snippetBuilder = new StringBuilder();

        for (String word : queryWords) {
            Pattern pattern = Pattern.compile("\\b.{5,10}" + word + "[а-я]*.{5,10}\\b");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String snippet = matcher.group();
                snippet = snippet.replace(word, "<b>"+word+"</b>");
                snippetBuilder.append(snippet).append(System.lineSeparator());
            }
        }

        return snippetBuilder.toString();
    }

    @Override
    public Float getPageRelevance(PageEntity page) {
        List<IndexEntity> indexes = page.getIndexes();
        Float relevance = 0.0f;

        for (IndexEntity index : indexes) {
            relevance += index.getRate();
        }

        return relevance;
    }

    @Override
    public List<PageEntity> getPagesByLemma(LemmaEntity lemma) {
        List<PageEntity> pages = new ArrayList<>();
        List<IndexEntity> indexes =  indexRepository.findAllByLemma(lemma.getId());
        Map<PageEntity, List<IndexEntity>> pageIndexes = indexes.stream()
                .collect(Collectors.groupingBy(IndexEntity::getPage));

        for (Map.Entry<PageEntity, List<IndexEntity>> entry : pageIndexes.entrySet()) {
            PageEntity page = entry.getKey();
            page.addAllIndexes(entry.getValue());
            pages.add(page);
        }

        return pages;
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
