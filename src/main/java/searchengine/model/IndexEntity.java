package searchengine.model;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "page_index", indexes = @Index(name = "unique_page_lemma", columnList = "lemma_id, page_id", unique = true))
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "lemma_id", referencedColumnName = "id", nullable = false)
    private LemmaEntity lemma;

    @ManyToOne
    @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false)
    private PageEntity page;

    @Column(precision = 5, scale = 2, nullable = false)
    private Float rate;

//    public void setLemma(LemmaEntity lemma) {
//        this.lemma = lemma;
//        lemma.getIndexes().add(this);
//    }
//
//    public void setPage(PageEntity page) {
//        this.page = page;
//        page.addIndex(this);
//    }
}
