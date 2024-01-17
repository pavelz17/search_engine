package searchengine.utils;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.JsoupConnection;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PageWalker extends RecursiveAction {
    private static final int MAX_PATH_LENGTH = 50;
    private static final String INTERRUPT_INDEXING_ERROR_MESSAGE = "Индексация остановлена пользователем";
    private static final String CONNECT_ERROR_MESSAGE = "Не удалось подключиться к странице";
    private final SiteEntity siteEntity;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final JsoupConnection jsoupConnection;
    private final String url;

    public PageWalker(SiteEntity siteEntity,
                      SiteRepository siteRepository,
                      PageRepository pageRepository,
                      JsoupConnection jsoupConnection,
                      String url) {
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.jsoupConnection = jsoupConnection;
        this.url = url;
    }

    @Override
    protected void compute() {
        try {
            siteRepository.updateStatusTime(LocalDateTime.now(), siteEntity.getId());
            Connection.Response response = getResponse(url);
            Document document = response.parse();
            Integer statusCode = response.statusCode();
            String html = document.html();
            String path = getPath();

            savePageToDatabase(statusCode, path, html);
            Set<String> innerLinks = getInnerLinks(document);
            if(!innerLinks.isEmpty()) {
                invokeAll(createSubTasks(innerLinks));
            }

        } catch (IOException e) {
            siteEntity.setLastError(CONNECT_ERROR_MESSAGE + e.getMessage());
            siteRepository.updateLastError(siteEntity.getLastError(), siteEntity.getId());
        } catch (InterruptedException | CancellationException e) {
            siteEntity.setLastError(INTERRUPT_INDEXING_ERROR_MESSAGE);
            siteRepository.updateLastError(siteEntity.getLastError(), siteEntity.getId());
            throw new RuntimeException();
        }
    }

    private Connection.Response getResponse(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(jsoupConnection.getUserAgent())
                .referrer(jsoupConnection.getReferrer())
                .execute();
    }

    private String getPath() {
        return url.replace(siteEntity.getUrl(), "/");
    }

    private void savePageToDatabase(Integer statusCode, String path, String html) {
        PageEntity pageEntity = PageEntity.builder()
                .code(statusCode)
                .path(path)
                .content(html)
                .build();
        siteEntity.addPage(pageEntity);
        pageRepository.save(pageEntity);
    }

    private Set<String> getInnerLinks(Document document) throws InterruptedException {
        Set<String> innerLinks = new HashSet<>();
        Elements links = (siteEntity.getUrl().equals(url)) ? document.select("a[href]")
                                                           : document.body().select("a[href]");
        for(Element link : links) {
            Thread.sleep((long) (50 + (Math.random() * 450)));
            String absHref = link.attr("abs:href");
            if(isValid(absHref)) {
                innerLinks.add(absHref);
            }
        }
        return innerLinks;
    }

    private boolean isValid(String link) {
        if (link.length() > url.length() + MAX_PATH_LENGTH) {
            return false;
        }
        Pattern pattern = Pattern.compile("^"+url+"[^#]+");
        Matcher matcher = pattern.matcher(link);
        return matcher.find();
    }

    private List<PageWalker> createSubTasks(Set<String> innerLinks) {
        List<PageWalker> subTasks = new ArrayList<>();
        for (String link : innerLinks) {
            subTasks.add(new PageWalker(siteEntity, siteRepository, pageRepository, jsoupConnection, link));
        }
        return subTasks;
    }
}
