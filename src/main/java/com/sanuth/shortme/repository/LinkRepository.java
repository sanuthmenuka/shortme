package com.sanuth.shortme.repository;


import com.sanuth.shortme.model.db.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<ShortLink, Long> {
    Optional<ShortLink> findByLongUrl(String url);
    Optional<ShortLink> findByShortCode(String shortCode);
}
