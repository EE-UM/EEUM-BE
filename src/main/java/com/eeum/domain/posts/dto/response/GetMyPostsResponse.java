package com.eeum.domain.posts.dto.response;

import java.util.List;

public record GetMyPostsResponse(

        Long postCount,
        List<GetMyPostResponse> getMyPostResponses
) {
}
