package com.eeum.domain.posts.docs;

import com.eeum.domain.posts.dto.request.CreatePostRequest;
import com.eeum.domain.posts.dto.request.UpdatePostRequest;
import com.eeum.domain.posts.dto.response.*;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Posts", description = "Posts API")
public interface PostsApi {

    @Operation(summary = "새 플레이리스트 생성", description = "새 플레이리스트(게시글)을 생성합니다.")
    ApiResponse<CreatePostResponse> createPost(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody @Valid CreatePostRequest createPostRequest
    );

    @Operation(summary = "플레이리스트 수정", description = "내 플레이리스트(게시글)를 수정합니다.")
    ApiResponse<UpdatePostResponse> updatePost(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody @Valid UpdatePostRequest updatePostRequest
    );

    @Operation(summary = "진행중인 플레이리스트(게시글) 조회", description = "진행 상태인 플레이리스트(게시글)을 무한 스크롤 방식으로 조회합니다.")
    ApiResponse<List<PostsReadInfiniteScrollResponse>> readAllInfiniteScrollIng(
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastPostId", required = false) Long lastPostId
    );

    @Operation(summary = "완료된 플레이리스트(게시글) 조회", description = "완료된 플레이리스트(게시글)을 무한 스크롤 방식으로 조회합니다.")
    ApiResponse<List<PostsReadInfiniteScrollResponse>> readAllInfiniteScrollDone(
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastPostId", required = false) Long lastPostId
    );

    @Operation(summary = "플레이리스트 삭제", description = "내가 작성한 플레이리스트(게시글)을 삭제합니다.")
    ApiResponse<String> delete(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    );

    @Operation(summary = "랜덤 스토리 조회", description = "흔들기 제스처로 랜덤 스토리를 조회합니다.")
    ApiResponse<ShowRandomStoryOnShakeResponse> showRandomStoryOnShake();

    @Operation(summary = "플레이리스트(게시글) 상세 조회", description = "플레이리스트(게시글) ID로 플레이리스트(게시글) 상세 내용을 조회합니다.")
    ApiResponse<PostsReadResponse> getPostById(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    );

    @Operation(summary = "내가 작성한 플레이리스트 조회", description = "내가 작성한 게시글 목록을 조회합니다.")
    ApiResponse<List<GetMyPostsResponse>> getMyPosts(
            @CurrentUser UserPrincipal userPrincipal
    );

    @Operation(summary = "플레이리스트 완료 처리", description = "특정 플레이리스트(게시글)을 완료 상태로 변경합니다.")
    ApiResponse<CompletePostResponse> completePost(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    );

    @Operation(summary = "내가 좋아요한 플레이리스트(게시글) 조회", description = "내가 좋아요를 누른 플레이리스트(게시글) 목록을 조회합니다.")
    ApiResponse<List<GetLikedPostsResponse>> getLikedPosts(
            @CurrentUser UserPrincipal userPrincipal
    );

    @Operation(summary = "내가 댓글 단 플레이리스트(게시글) 조회", description = "내가 댓글을 단 게시글 목록을 조회합니다.")
    ApiResponse<List<GetCommentedPostsResponse>> getCommentedPosts(
            @CurrentUser UserPrincipal userPrincipal
    );
}
