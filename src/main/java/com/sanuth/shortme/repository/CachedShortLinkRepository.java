package com.sanuth.shortme.repository;

import com.sanuth.shortme.model.cache.CachedShortLink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CachedShortLinkRepository extends CrudRepository<CachedShortLink, String> {
    Optional<CachedShortLink> findByLongUrl(String longUrl);
}
