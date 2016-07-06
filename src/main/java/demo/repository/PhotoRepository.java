package demo.repository;

import demo.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by daniel on 06.07.16.
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
