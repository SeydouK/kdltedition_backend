package com.kdlt.platform.quote.repository;

import com.kdlt.platform.quote.entity.Quote;
import com.kdlt.platform.quote.entity.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByUserIdOrderByDateCreationDesc(Long userId);
    List<Quote> findByStatusOrderByDateCreationDesc(QuoteStatus status);
    List<Quote> findAllByOrderByDateCreationDesc();
}