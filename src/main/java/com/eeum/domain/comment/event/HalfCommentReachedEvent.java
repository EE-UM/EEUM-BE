package com.eeum.domain.comment.event;

public record HalfCommentReachedEvent(
    Long postId,
    Long postAuthorId,
    String postTitle
) {

}
