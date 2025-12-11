package com.eeum.domain.common.spamfilter.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "forbidden_words",
        indexes = {
            @Index(name = "idx_language", columnList = "language")
        }
)
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ForbiddenWords {

    @Id
    @Tsid
    private Long id;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String word;
}
