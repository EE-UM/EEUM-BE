package com.eeum.posts.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "albums")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album {

    @Id
    private Long id;

    private String songTitle;

    private String artistName;

    private String songImgUrl;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Posts posts;

    public static Album of(Long id, String songTitle, String artistName, String songImgUrl, Posts posts) {
        return Album.builder()
                .id(id)
                .songTitle(songTitle)
                .artistName(artistName)
                .songImgUrl(songImgUrl)
                .posts(posts)
                .build();
    }

    @Builder
    private Album(Long id, String songTitle, String artistName, String songImgUrl, Posts posts) {
        this.id = id;
        this.songTitle = songTitle;
        this.artistName = artistName;
        this.songImgUrl = songImgUrl;
        this.posts = posts;
    }
}
