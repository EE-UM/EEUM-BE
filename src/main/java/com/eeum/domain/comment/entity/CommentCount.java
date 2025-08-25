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

    public boolean hitLimit() {
        return commentCount.equals(commentCountLimit);
    }
}
