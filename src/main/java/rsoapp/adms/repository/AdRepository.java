package rsoapp.adms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rsoapp.adms.model.entity.Ad;

import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Integer> {

    List<Ad> getAllByUserId(Integer userId);

    @Query("SELECT a FROM Ad a WHERE LOWER(a.title) LIKE %?1%")
    List<Ad> searchAds(String keyword);
}
