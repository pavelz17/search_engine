package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "lemma", indexes = @Index(name = "unique_lemma", columnList = "lemma, site_id", unique = true))
public class LemmaEntity {
    private final static int INDEX_CAPACITY = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private SiteEntity site;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @Column(columnDefinition = "integer  default 1", nullable = false)
    private Integer frequency;

    @OneToMany(mappedBy = "lemma")
    private final List<IndexEntity> indexes = new ArrayList<>(INDEX_CAPACITY);

    public List<IndexEntity> getIndexes() {
        return new ArrayList<>(indexes);
    }

    public void addIndex(IndexEntity index) {
        indexes.add(index);
    }
}

