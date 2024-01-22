package searchengine.model;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "indexes")
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    private LemmaEntity lemma;

    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity page;

    @Column(precision = 5, scale = 2, nullable = false)
    private Float kef;
}
