package com.eeum.domain.posts.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostsTest {

    private static Posts createTestPost() {
        Album album = Album.of("앨범명", "노래명", "아티스트명", "http://artwork.url", "http://apple.music/url");
        return Posts.of("제목", "내용", album, 1L);
    }

    @Test
    @DisplayName("Posts.of 생성 시 isCompleted=false, isDeleted=false 기본값 설정")
    void of_createsWithDefaultFlags() {
        Posts post = createTestPost();

        assertThat(post.getIsCompleted()).isFalse();
        assertThat(post.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Posts.of 생성 시 전달한 title, content, userId 가 저장됨")
    void of_storesFields() {
        Album album = Album.of("앨범", "노래", "아티스트", "url1", "url2");
        Posts post = Posts.of("내 제목", "내 내용", album, 42L);

        assertThat(post.getTitle()).isEqualTo("내 제목");
        assertThat(post.getContent()).isEqualTo("내 내용");
        assertThat(post.getUserId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("update 호출 시 title 과 content과 album이 변경됨")
    void update_changesFields() {
        // given
        Posts post = createTestPost();
        Album album = Album.of("새 앨범이름", "새 음악이름", "새 아티스트이름", "새 아트워크url", "새 applemusicurl");

        // when
        post.update("새 제목", "새 내용", album);

        // then
        assertThat(post.getTitle()).isEqualTo("새 제목");
        assertThat(post.getContent()).isEqualTo("새 내용");
        assertThat(post.getAlbum().getAlbumName()).isEqualTo("새 앨범이름");
    }

    @Test
    @DisplayName("updateIsCompleted 호출 시 isCompleted 가 true 로 변경됨")
    void updateIsCompleted_setsTrue() {
        Posts post = createTestPost();
        assertThat(post.getIsCompleted()).isFalse();

        post.updateIsCompleted();

        assertThat(post.getIsCompleted()).isTrue();
    }

    @Test
    @DisplayName("softDelete 호출 시 isDeleted 가 true 로 변경됨")
    void softDelete_setsIsDeletedTrue() {
        Posts post = createTestPost();
        assertThat(post.isDeleted()).isFalse();

        post.softDelete();

        assertThat(post.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("updateCompletionType 호출 시 completionType 이 설정됨")
    void updateCompletionType_setsType() {
        Posts post = createTestPost();

        post.updateCompletionType(CompletionType.AUTO_COMPLETION);

        assertThat(post.getCompletionType()).isEqualTo(CompletionType.AUTO_COMPLETION);
    }
}
