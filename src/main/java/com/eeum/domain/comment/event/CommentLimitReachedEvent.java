package com.eeum.domain.comment.event;

public record CommentLimitReachedEvent(
    Long postId,
    Long postAuthorId,
    String postTitle
) {

}
