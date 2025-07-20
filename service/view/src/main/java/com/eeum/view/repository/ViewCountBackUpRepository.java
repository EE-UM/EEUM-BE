package com.eeum.view.repository;

import com.eeum.view.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ViewCountBackUpRepository extends JpaRepository<View, Long> {

    @Query(
            value = "update view set view_count = :viewCount " +
                    "where post_id = :postId and view_count < :viewCount",
            nativeQuery = true
    )
    @Modifying
    int updateViewCount(
            @Param("postId") Long postId,
            @Param("viewCount") Long viewCount
    );
}
