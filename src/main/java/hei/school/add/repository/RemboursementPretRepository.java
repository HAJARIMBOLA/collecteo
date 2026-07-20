package hei.school.add.repository;

import hei.school.add.entity.RemboursementPret;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemboursementPretRepository extends JpaRepository<RemboursementPret, Long> {
  List<RemboursementPret> findByPretIdOrderByDateDesc(Long pretId);
}
