package com.eeum.domain.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Table(name = "comment_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentCount {

    @Id
    private Long postId;
    private Long commentCount;
    private Long commentCountLimit;

    public static CommentCount of(Long postId, Long count, Long commentCountLimit) {
        CommentCount commentCount = new CommentCount();
        commentCount.postId = postId;
        commentCount.commentCount = count;
        commentCount.commentCountLimit = commentCountLimit;
        return commentCount;
    }
}
