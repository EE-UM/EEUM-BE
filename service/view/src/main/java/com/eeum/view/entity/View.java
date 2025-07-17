package com.eeum.view.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Table(name = "view")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class View {

    @Id
    private Long postId;

    private Long viewCount;

    public static View of(Long postId, Long viewCount) {
        return View.builder()
                .postId(postId)
                .viewCount(viewCount)
                .build();
    }

    @Builder
    private View(Long postId, Long viewCount) {
        this.postId = postId;
        this.viewCount = viewCount;
    }
}
