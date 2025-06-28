package com.eeum.posts.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album {

    private String albumName;

    private String songName;

    private String artistName;

    private String artworkUrl;

    public static Album of(String albumName, String songName, String artistName, String artworkUrl) {
        return Album.builder()
                .albumName(albumName)
                .songName(songName)
                .artistName(artistName)
                .artworkUrl(artworkUrl)
                .build();
    }

    @Builder
    private Album(String albumName, String songName, String artistName, String artworkUrl) {
        this.albumName = albumName;
        this.songName = songName;
        this.artistName = artistName;
        this.artworkUrl = artworkUrl;
    }
}
