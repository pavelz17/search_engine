package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "site")
@Builder
@Entity
@Table(name = "page", indexes = @Index(name = "path_index", columnList = "path"))
public class PageEntity {
    private final static int INDEX_CAPACITY = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private SiteEntity site;

    @Column(columnDefinition = "VARCHAR(55)", nullable = false)
    private String path;

    @Column(nullable = false)
    private Integer code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "page")
    private final List<IndexEntity> indexes = new ArrayList<>(INDEX_CAPACITY);

    public List<IndexEntity> getIndexes() {
        return new ArrayList<>(indexes);
    }

    public void addIndex(IndexEntity index) {
        indexes.add(index);
    }
}

