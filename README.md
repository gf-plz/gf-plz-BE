# GF Plz Backend

AI 여자친구 챗봇 애플리케이션 백엔드 서버

## 기술 스택

### Backend
- **Spring Boot** 3.5.7
- **Java** 21
- **JPA / Hibernate**
- **REST API**
- **AWS EC2** 배포
- **H2 Database** (파일 기반, MySQL 모드)

### AI / LLM
- **Groq API**
  - 모델: `llama-3.3-70b-versatile`
  - API 엔드포인트: `https://api.groq.com/openai/v1/chat/completions`
  - 최대 토큰: 1024
  - Temperature: 0.7
- **Google Cloud TTS** (Text-to-Speech)
  - 음성 합성 기능
- **텍스트/음성 대화 분석**
- **프롬프트 엔지니어링 기반 한줄평 생성**

## 주요 기능

- AI 캐릭터 기반 대화 생성
- 텍스트 및 음성 대화 지원
- 대화 히스토리 관리
- 캐릭터별 한줄평 자동 생성

## 환경 변수

프로젝트 루트에 `.env` 파일을 생성하고 다음 환경 변수를 설정하세요:

```
GROQ_API_KEY=your_groq_api_key_here
GOOGLE_APPLICATION_CREDENTIALS=./google-credentials.json
GOOGLE_CLOUD_PROJECT_ID=your_project_id
```

## 실행 방법

```bash
# 애플리케이션 실행
./gradlew bootRun

# 또는
gradlew.bat bootRun  # Windows
```

## 데이터베이스

- H2 콘솔: `http://localhost:8080/h2-console`
- 로컬 데이터베이스 파일: `./data/gfplz.mv.db`

