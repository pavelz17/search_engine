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
    private final List<PageEntity> pages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "site")
    private final List<LemmaEntity> lemmas = new ArrayList<>();

    public List<PageEntity> getPages() {
        return new ArrayList<>(pages);
    }

    public List<LemmaEntity> getLemmas() {
        return new ArrayList<>(lemmas);
    }

    public void addPage(PageEntity page) {
        pages.add(page);
        page.setSite(this);
    }

    public void addLemma(LemmaEntity lemma) {
        lemmas.add(lemma);
        lemma.setSite(this);
    }
}
