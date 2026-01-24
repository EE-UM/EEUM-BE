package com.eeum.domain.posts.dto.response;

import java.util.List;

public record GetLikedPostsWithSizeResponse(
        long likedPostsSize,
        List<GetLikedPostsResponse> getLikedPostsResponses
) {
}
