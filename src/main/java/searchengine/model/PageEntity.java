package searchengine.model;

import lombok.*;

import javax.persistence.*;


@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "page")
public class PageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String path;

    @Column(nullable = false)
    private Integer code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
}
