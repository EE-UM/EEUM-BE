package com.eeum.domain.comment.entity;

import com.eeum.domain.comment.exception.AlreadyFinishedPostException;
import jakarta.persistence.*;
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

    @Version
    @Column(nullable = false)
    private Long version;

    public static CommentCount of(Long postId, Long count, Long commentCountLimit) {
        CommentCount commentCount = new CommentCount();
        commentCount.postId = postId;
        commentCount.commentCount = count;
        commentCount.commentCountLimit = commentCountLimit;
        return commentCount;
    }

    public void increaseOrThrow() {
        if (commentCount >= commentCountLimit) {
            throw new AlreadyFinishedPostException("comment limit reached.");
        }
        this.commentCount += 1;
    }

    public void decreaseSafely() {
        if (commentCount > 0) {
            this.commentCount -= 1;
        }
    }

    /**
     * 목표 댓글 수의 절반 (올림).
     */
    public long halfOfLimit() {
        return (commentCountLimit + 1) / 2;
    }

    public boolean hitLimit() {
        return commentCount.equals(commentCountLimit);
    }
}
