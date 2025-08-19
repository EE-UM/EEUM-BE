package com.eeum.domain.view.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Table(name = "post_view_count")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostViewCount {

    @Id
    private Long postId;
    private Long viewCount;

    public static PostViewCount init(Long postId, Long viewCount) {
        PostViewCount postViewCount = PostViewCount.builder()
                .postId(postId)
                .viewCount(viewCount)
                .build();
        return postViewCount;
    }

    @Builder
    private PostViewCount(Long postId, Long viewCount) {
        this.postId = postId;
        this.viewCount = viewCount;
    }
}
