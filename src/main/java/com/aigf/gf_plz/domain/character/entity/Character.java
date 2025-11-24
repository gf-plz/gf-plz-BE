package com.aigf.gf_plz.domain.character.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 캐릭터 엔티티
 */
@Entity
@Table(name = "Character")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "캐릭터ID")
    private Long characterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "상태ID", nullable = false)
    private Status status;

    @Column(name = "캐릭터 소개", columnDefinition = "TEXT")
    private String description;

    @Column(name = "이미지", length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "목소리", nullable = false)
    private VoiceType voiceType;

    @CreationTimestamp
    @Column(name = "생성 날짜", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "변경 날짜", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Character(Status status, String description, String imageUrl, VoiceType voiceType) {
        this.status = status;
        this.description = description;
        this.imageUrl = imageUrl;
        this.voiceType = voiceType;
    }

    /**
     * 캐릭터의 성격 프롬프트를 생성합니다.
     * "너는 ENFJ 테토 80%의 안정형 여자친구야" 형식으로 반환합니다.
     */
    public String generatePersonalityPrompt() {
        Status status = this.status;
        String mbti = status.getMbti().name();
        String teto = status.getTeto() + "%";
        String attachment = status.getAttachment().name();
        String gender = status.getGender() == Gender.여 ? "여자친구" : "남자친구";
        String name = status.getName();

        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("너는 %s 테토 %s의 %s %s야. 이름은 %s이야.", 
                mbti, teto, attachment, gender, name));
        
        // 캐릭터 소개가 있으면 추가
        if (this.description != null && !this.description.isBlank()) {
            prompt.append("\n").append(this.description);
        }
        
        return prompt.toString();
    }
}
