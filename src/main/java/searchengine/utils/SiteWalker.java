package searchengine.utils;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.SiteService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@Getter
public class SiteWalker extends RecursiveAction {
    private static final int MAX_PATH_LENGTH = 50;
    private static final String INTERRUPT_INDEXING_ERROR_MESSAGE = "Индексация остановлена пользователем";
    private static final String CONNECT_ERROR_MESSAGE = "Не удалось подключиться к странице: ";
    private final SiteEntity siteEntity;
    private final SiteService siteService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String url;

    public SiteWalker(SiteEntity siteEntity,
                      SiteService siteService,
                      String url) {
        this.siteEntity = siteEntity;
        this.siteService = siteService;
        this.url = url;
    }

    @Override
    protected void compute() {
        System.out.println(url);
        try {
            siteService.updateStatusTime(LocalDateTime.now(), siteEntity.getId());
            Connection.Response response = siteService.getResponse(url);
            Document document = response.parse();
            Integer statusCode = response.statusCode();
            String html = document.html();
            String path = url.replace(siteEntity.getUrl(), "/");
            PageEntity pageEntity = PageEntity.builder()
                    .code(statusCode)
                    .path(path)
                    .content(html)
                    .build();
            siteEntity.addPage(pageEntity);
            Set<String> innerLinks = getInnerLinks(document);
            if(!innerLinks.isEmpty()) {
                invokeAll(createSubTasks(innerLinks));
            }

        } catch (IOException e) {
            siteEntity.setLastError(CONNECT_ERROR_MESSAGE + url);
            siteService.updateLastError(siteEntity.getLastError(), siteEntity.getId());
        } catch (InterruptedException | CancellationException e) {
            siteEntity.setLastError(INTERRUPT_INDEXING_ERROR_MESSAGE);
            siteService.updateLastError(siteEntity.getLastError(), siteEntity.getId());
            throw new RuntimeException();
        }
    }

    private Set<String> getInnerLinks(Document document) throws InterruptedException {
        Set<String> innerLinks = new HashSet<>();
        Elements links = document.body().select("a[href^=/]");
        for(Element link : links) {
            Thread.sleep((long) (50 + (Math.random() * 450)));
            String absLink = link.attr("abs:href");
            if(checkLink(absLink)) {
                System.out.println(absLink);
                innerLinks.add(absLink);
            }
        }
        return innerLinks;
    }

    private boolean checkLink(String link) {
        if (link.replace(siteEntity.getUrl(), "/").length() > MAX_PATH_LENGTH) {
            return false;
        }
        return Pattern.matches("^"+url+"[^#]+((?<!\\.\\w{3,5})|(?<=\\.html))$", link);
    }

    private List<SiteWalker> createSubTasks(Set<String> innerLinks) {
        List<SiteWalker> subTasks = new ArrayList<>();
        for (String link : innerLinks) {
            subTasks.add(new SiteWalker(siteEntity, siteService, link));
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
