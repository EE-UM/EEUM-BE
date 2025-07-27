package com.eeum.domain.posts.entity;

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

    private String appleMusicUrl;

    public static Album of(String albumName, String songName, String artistName, String artworkUrl, String appleMusicUrl) {
        return Album.builder()
                .albumName(albumName)
                .songName(songName)
                .artistName(artistName)
                .artworkUrl(artworkUrl)
                .appleMusicUrl(appleMusicUrl)
                .build();
    }

    @Builder
    private Album(String albumName, String songName, String artistName, String artworkUrl, String appleMusicUrl) {
        this.albumName = albumName;
        this.songName = songName;
        this.artistName = artistName;
        this.artworkUrl = artworkUrl;
        this.appleMusicUrl = appleMusicUrl;
    }
}
