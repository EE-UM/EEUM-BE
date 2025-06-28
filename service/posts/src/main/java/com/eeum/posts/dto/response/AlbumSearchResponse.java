package com.eeum.posts.dto.response;

public record AlbumSearchResponse(
        String albumName,
        String songName,
        String artistName,
        String artworkUrl
) {
}
