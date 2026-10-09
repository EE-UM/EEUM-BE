package com.eeum.domain.comment.event;

public record FirstCommentCreatedEvent(
    Long postId,
    Long postAuthorId,
    String postTitle
) {

}
