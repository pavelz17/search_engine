package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@ToString(exclude = "pages")
@Table(name = "site")
public class SiteEntity {

    @Transient
    private static final int PAGES_CAPACITY = 200;

    @Transient
    private static final int LEMMAS_CAPACITY = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    @Enumerated(EnumType.STRING)
    private SearchStatus status;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "site")
    private final List<PageEntity> pages = new ArrayList<>(PAGES_CAPACITY);

    @Builder.Default
    @OneToMany(mappedBy = "site")
    private final List<LemmaEntity> lemmas = new ArrayList<>(PAGES_CAPACITY);

    public List<PageEntity> getPages() {
        return new ArrayList<>(pages);
    }

    public List<PageEntity> getPages(int startIndex, int endIndex) {
        return new ArrayList<>(pages.subList(startIndex, endIndex));
    }

    public int getPagesSize() {
        return pages.size();
    }

    public void addPage(PageEntity pageEntity) {
        pages.add(pageEntity);
        pageEntity.setSite(this);
    }

    public List<LemmaEntity> getLemmas() {
        return new ArrayList<>(lemmas);
    }

    public void addLemma(LemmaEntity lemmaEntity) {
        lemmas.add(lemmaEntity);
        lemmaEntity.setSite(this);
    }
}
