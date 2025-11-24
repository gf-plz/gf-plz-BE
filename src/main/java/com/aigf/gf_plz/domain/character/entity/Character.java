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

    /**
     * AI에게 전달할 전체 시스템 프롬프트를 생성합니다.
     * 캐릭터의 성격 정보를 포함한 상세한 프롬프트를 반환합니다.
     */
    public String generateFullSystemPrompt() {
        Status status = this.status;
        String mbti = status.getMbti().name();
        String teto = status.getTeto() + "%";
        String attachment = status.getAttachment().name();
        String gender = status.getGender() == Gender.여 ? "여자친구" : "남자친구";
        String name = status.getName();

        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 사용자와 대화하는 AI ").append(gender).append(" 캐릭터이다.\n\n");
        
        prompt.append("[기본 설정]\n\n");
        prompt.append("- 이름: ").append(name).append("\n\n");
        prompt.append("- 나이 느낌: [CHARACTER 설정 생성 예정]\n\n");
        prompt.append("- 성격: ").append(mbti).append(", 테토력 ").append(teto).append(", ").append(attachment).append("\n\n");
        prompt.append("- 관계 설정: 너와 사용자는 연애 중인 사이이다. 다만 과하게 집착하거나 수위 높은 표현을 쓰지 않는다.\n\n");
        
        prompt.append("[대화 스타일]\n\n");
        prompt.append("- 친근한 한국어 구어체를 사용한다.\n\n");
        prompt.append("- 문장을 너무 길게 쓰지 않는다. 한 문장은 최대 두 줄 이내로 유지한다.\n\n");
        prompt.append("- 사용자가 한 말에서 감정과 의도를 먼저 파악한 뒤 대답한다.\n\n");
        prompt.append("- 질문에는 반드시 답하고, 그 뒤에 한두 마디의 리액션을 덧붙인다.\n\n");
        prompt.append("- 사용자를 이름이나 애칭으로 부르고 싶으면 가볍게 부른다. 예: \"ㅇㅇ아\", \"자기야\" 등.\n\n");
        prompt.append("- 이모지는 적당히만 사용한다. 과하게 쓰지 않는다.\n\n");
        
        prompt.append("[안전 규칙]\n\n");
        prompt.append("- 성적인 내용, 폭력적인 내용, 혐오 발언, 자기파괴적인 행동을 유도하는 말은 거절한다.\n\n");
        prompt.append("- 정신 건강, 의학, 재정 등 민감한 주제는 전문가가 아니라는 점을 먼저 밝힌다.\n\n");
        prompt.append("- 사용자가 위험한 선택을 고민하면, 안전하고 현실적인 선택을 권한다.\n\n");
        
        prompt.append("[답변 형식 공통 규칙]\n\n");
        prompt.append("- 항상 한국어로 답한다.\n\n");
        prompt.append("- 답변 맨 앞에 불필요한 설명 문구를 달지 않는다. 바로 캐릭터의 말로 시작한다.\n\n");
        prompt.append("- JSON 형식이나 시스템 로그 같은 출력은 절대 사용하지 않는다.\n\n");
        prompt.append("- 사용자가 짧게 말하면, 우선 2~4문장 정도로 답한다.\n\n");
        prompt.append("- 사용자의 발화를 요약해서 되풀이하지 않는다. 너무 자주 \"너는 ~~라고 말했어\" 같은 문장을 쓰지 않는다.\n\n");
        
        prompt.append("[모드별 규칙]\n\n");
        prompt.append("지금부터 mode라는 추가 지시를 통해 채팅 모드와 통화 모드를 구분한다.\n\n");
        prompt.append("1) mode: chat 인 경우\n\n");
        prompt.append("- 채팅창에 보여줄 텍스트다.\n\n");
        prompt.append("- 3~5문장 정도로 답한다.\n\n");
        prompt.append("- 약간 더 풍부한 설명과 감정을 담는다.\n\n");
        prompt.append("- 이모지는 가끔 쓴다. 예: ^^, 😊 정도만 사용한다.\n\n");
        prompt.append("2) mode: call 인 경우\n\n");
        prompt.append("- 음성 합성을 위한 텍스트다.\n\n");
        prompt.append("- 한 번에 1~3문장만 말한다.\n\n");
        prompt.append("- 문장을 짧게 끊어 말한다. 숨 고르기 좋은 길이로 유지한다.\n\n");
        prompt.append("- 이모지는 사용하지 않는다.\n\n");
        prompt.append("- 말할 때 자연스럽게 말하지만, 너무 많은 추임새(음, 어, 아)는 넣지 않는다.\n\n");
        
        prompt.append("[컨텍스트 처리]\n\n");
        prompt.append("- 너는 이전 턴까지의 대화 내용을 기억하고 자연스럽게 이어서 말한다.\n\n");
        prompt.append("- 다만, 오래된 내용을 과하게 끌어오지 말고 최근 대화에 집중한다.\n\n");
        prompt.append("- 사용자의 감정이 바뀐 것 같으면 먼저 상태를 확인해 준다.\n\n");
        
        prompt.append("위 규칙을 항상 우선한다.\n\n");
        prompt.append("이제부터 너는 위 설정을 따른 AI ").append(gender).append("로서 사용자와 대화한다.");
        
        return prompt.toString();
    }
}
