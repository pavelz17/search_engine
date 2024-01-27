package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConf;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.SiteWalker;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {
    private static final int BATCH_SIZE_FOR_SAVE_PAGE = 15;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final JsoupConf jsoupConf;

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
    public PageEntity savePage(SiteEntity site, String url, Connection.Response response) throws IOException {
        PageEntity pageEntity = PageEntity.builder()
                .code(response.statusCode())
                .content(response.parse().html())
                .path(url.replace(site.getUrl(), "/"))
                .build();
        site.addPage(pageEntity);
        return pageRepository.save(pageEntity);
    }

    @Override
    public void saveLemmas(SiteEntity site, Map<String, Integer> lemmasQuantity) {
        for (Map.Entry<String, Integer> entry : lemmasQuantity.entrySet()) {
            LemmaEntity lemma = LemmaEntity.builder()
                    .lemma(entry.getKey())
                    .repeatCount(Float.valueOf(entry.getValue()))
                    .site(site)
                    .build();
            site.addLemma(lemma);
        }
        lemmaRepository.saveAll(site.getLemmas());
    }

    @Override
    public void saveIndexes(SiteEntity site, PageEntity page) {
        List<IndexEntity> indexes = new ArrayList<>();
        for (LemmaEntity lemma : site.getLemmas()) {
            IndexEntity index = IndexEntity.builder()
                    .page(page)
                    .lemma(lemma)
                    .rate(lemma.getRepeatCount())
                    .build();
        }
        indexRepository.saveAll(indexes);
    }

    @Override
    public void deleteSiteByUrl(String url) {
        siteRepository.deleteByUrl(url);
    }

    @Override
    public void deletePageById(Integer id) {
        List<IndexEntity> indexes = indexRepository.findAllByPageId(id);
        List<LemmaEntity> deleteLemmas = new ArrayList<>();
        List<LemmaEntity> updateLemmas = new ArrayList<>();
        for (IndexEntity index : indexes) {
            LemmaEntity lemma = index.getLemma();
            int frequency = lemma.getFrequency();
            if (frequency > 1) {
                updateLemmas.add(lemma);
            } else {
                deleteLemmas.add(lemma);
            }
        }
        if (!deleteLemmas.isEmpty()) {
            lemmaRepository.deleteAllLemmasByIds(deleteLemmas);
        }
        if (!updateLemmas.isEmpty()) {
            lemmaRepository.updateFrequencyAllLemmasByIds(updateLemmas);
        }

        pageRepository.deleteById(id);
    }

    @Override
    public Optional<SiteEntity> findSiteByUrl(String url) {
        Pattern pattern = Pattern.compile("^https?://\\w+.(ru|com|org)/");
        Matcher matcher = pattern.matcher(url);
        String rootUrl = "";

        while (matcher.find()) {
            rootUrl = matcher.group();
        }
        return siteRepository.findByUrl(rootUrl);
    }

    @Override
    public Optional<PageEntity> findPageByPath(String path) {
        return pageRepository.findByPath(path);
    }

    @Override
    public void walkAndSavePages(SiteEntity site, ExecutorService executorService, ForkJoinPool forkJoinPool) {
        SiteWalker siteWalker = new SiteWalker(site, this, site.getUrl());
        executorService.execute(() -> {
            try {
                siteWalker.setRunning(true);
                forkJoinPool.invoke(siteWalker);
                siteRepository.updateSearchStatus(SearchStatus.INDEXED.name(), site.getId());
                pageRepository.saveAll(site.getPages());
            } catch (RuntimeException e) {
                siteRepository.updateSearchStatus(SearchStatus.FAILED.name(), site.getId());
                pageRepository.saveAll(site.getPages());
            } finally {
                siteWalker.setRunning(false);
                executorService.shutdown();
            }
        });

        new Thread(() -> {
            int index = 0;
            while (siteWalker.getRunning()) {
                if(site.getPagesSize() >= index + BATCH_SIZE_FOR_SAVE_PAGE) {
                    pageRepository.saveAll(site.getPages(index, index + BATCH_SIZE_FOR_SAVE_PAGE));
                    index += BATCH_SIZE_FOR_SAVE_PAGE;
                }
            }

            int pagesSize = site.getPagesSize();
            if (pagesSize > index) {
                pageRepository.saveAll(site.getPages(index, pagesSize));
            }
        }).start();
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
    public Connection.Response getResponse(String url) throws IOException {
        return Jsoup.connect(url).userAgent(jsoupConf.getUserAgent())
                .referrer(jsoupConf.getReferrer())
                .execute();
    }
}
