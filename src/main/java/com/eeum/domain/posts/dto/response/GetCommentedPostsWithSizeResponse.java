package com.eeum.domain.posts.dto.response;

import java.util.List;

public record GetCommentedPostsWithSizeResponse(

        long CommentedPostsCount,
        List<GetCommentedPostsResponse> getCommentedPostsResponses
) {
}
