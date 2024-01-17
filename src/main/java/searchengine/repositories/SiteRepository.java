package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Repository
public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM site s WHERE s.url LIKE :url%", nativeQuery = true)
    void deleteByUrl(String url);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site s SET s.status_time = :statusTime WHERE s.id = :id ", nativeQuery = true)
    void updateStatusTime(LocalDateTime statusTime, Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site s SET s.status = :status WHERE s.id = :id ", nativeQuery = true)
    void updateSearchStatus(String status, Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site s SET s.last_error = :error WHERE s.id = :id ", nativeQuery = true)
    void updateLastError(String error, Integer id);
}
