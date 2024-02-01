package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConf;
import searchengine.config.Site;
import searchengine.config.SitesList;
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
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final JsoupConf jsoupConf;
    private final SitesList sitesFromConfigFile;

    @Override
    public List<Site> getSitesFromConfig() {
        return sitesFromConfigFile.getSites();
    }

    @Override
    public void updateSitesFromConfigFile() {
        for (Site site : sitesFromConfigFile.getSites()) {
            if (this.findSiteByUrl(site.getUrl()).isEmpty()) {
                this.saveSite(site.getUrl(), site.getName());
            }
        }
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
    public void saveLemmasAndIndexes(HashMap<String, Integer> lemmas, SiteEntity site, PageEntity page) {
        Set<Integer> updateLemmasIds = new HashSet<>();
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
        LemmaEntity lemmaEntity;
        String lemma = entry.getKey();
            Optional<LemmaEntity> maybeLemma = lemmaRepository.findByUniqueKey(lemma, site.getId());
            if (maybeLemma.isPresent()) {
                lemmaEntity = maybeLemma.get();
                updateLemmasIds.add(lemmaEntity.getId());
            } else {
                lemmaEntity = LemmaEntity.builder()
                        .lemma(entry.getKey())
                        .site(site)
                        .frequency(1)
                        .build();
                lemmaRepository.save(lemmaEntity);
            }

            IndexEntity index = IndexEntity.builder()
                    .lemma(lemmaEntity)
                    .page(page)
                    .rate(Float.valueOf(entry.getValue()))
                    .build();
            indexRepository.save(index);
        }
        lemmaRepository.updateFrequencyAfterSave(updateLemmasIds);
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
