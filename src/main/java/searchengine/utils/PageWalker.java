package searchengine.utils;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.model.PageDto;
import searchengine.model.SiteEntity;
import searchengine.services.IndexingSiteService;
import searchengine.services.SiteService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@Getter
public class PageWalker extends RecursiveAction {
    private static final int MAX_PATH_LENGTH = 50;
    private static final String INTERRUPT_INDEXING_ERROR_MESSAGE = "Индексация остановлена пользователем";
    private static final String CONNECT_ERROR_MESSAGE = "Не удалось подключиться к странице: ";
    private final SiteEntity site;
    private final SiteService siteService;
    private final IndexingSiteService indexingSiteService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String url;

    public PageWalker(String url,
                      SiteEntity site,
                      SiteService siteService,
                      IndexingSiteService indexingSiteService) {
        this.url = url;
        this.site = site;
        this.siteService = siteService;
        this.indexingSiteService = indexingSiteService;
    }

    @Override
    protected void compute() {
        try {
            Connection.Response response = siteService.getResponse(url);
            Document document = response.parse();
            int statusCode = response.statusCode();
            PageDto pageDto = new PageDto(statusCode, document.html());
            indexingSiteService.createIndex(site, url, pageDto);
            Set<String> innerLinks = getInnerLinks(document);
            if(!innerLinks.isEmpty()) {
                invokeAll(createSubTasks(innerLinks));
            }
        } catch (IOException e) {
            site.setLastError(CONNECT_ERROR_MESSAGE + url);
            siteService.updateLastError(site.getLastError(), site.getId());
        } catch (InterruptedException | CancellationException e) {
            site.setLastError(INTERRUPT_INDEXING_ERROR_MESSAGE);
            siteService.updateLastError(site.getLastError(), site.getId());
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> getInnerLinks(Document document) throws InterruptedException {
        Set<String> innerLinks = new HashSet<>();
        Elements links = document.body().select("a[href^=/]");
        for(Element link : links) {
            Thread.sleep((long) (50 + (Math.random() * 450)));
            String absLink = link.attr("abs:href");
            if(checkLink(absLink)) {
                innerLinks.add(absLink);
            }
        }
        return innerLinks;
    }

    private boolean checkLink(String link) {
        if (link.replace(site.getUrl(), "/").length() > MAX_PATH_LENGTH) {
            return false;
        }
        return Pattern.matches("^"+url+"[^#]+((?<!\\.\\w{3,5})|(?<=\\.html))$", link);
    }

    private List<PageWalker> createSubTasks(Set<String> innerLinks) {
        List<PageWalker> subTasks = new ArrayList<>();
        for (String link : innerLinks) {
            subTasks.add(new PageWalker(link, site, siteService, indexingSiteService));
        }
        return subTasks;
    }

    public boolean getRunning() {
        return running.get();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }
}
