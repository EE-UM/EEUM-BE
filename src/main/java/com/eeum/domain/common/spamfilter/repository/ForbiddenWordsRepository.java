package com.eeum.domain.common.spamfilter.repository;

import com.eeum.domain.common.spamfilter.entity.ForbiddenWords;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForbiddenWordsRepository extends JpaRepository<ForbiddenWords, Long> {
    Optional<ForbiddenWords> findByWord(String word);

    @Query(
            value = "select f.word " +
                    "from forbidden_words f " +
                    "where f.word in :words ",
            nativeQuery = true
    )
    List<String> findIn(@Param("words") List<String> words);
}
