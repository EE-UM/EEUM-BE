package com.eeum.domain.posts.dto.response;

public record AlbumSearchResponse(
        String albumName,
        String songName,
        String artistName,
        String artworkUrl,
        String previewMusicUrl
) {

    public static AlbumSearchResponse of(String albumName,
                                         String songName,
                                         String artistName,
                                         String artworkUrl,
                                         String previewMusicUrl) {
        return new AlbumSearchResponse(albumName, songName, artistName, artworkUrl, previewMusicUrl);
    }
}
