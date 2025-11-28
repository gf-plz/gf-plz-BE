# API 변경사항 요약

## 현재 구현된 API 목록

### 1. Character API (`/api/characters`)
- ✅ `POST /api/characters` - 캐릭터 생성 (새로 추가)
- ✅ `GET /api/characters/{characterId}` - 캐릭터 조회 (새로 추가)
- ✅ `GET /api/characters/recent` - 최근 캐릭터 조회 (새로 추가)
- ✅ `POST /api/characters/{characterId}/select` - 캐릭터 선택 및 세션 생성 (새로 추가)

### 2. Chat API (`/api/chat`)
- ✅ `POST /api/chat` - 텍스트 채팅 (변경 없음)

### 3. Call API (`/api/call`)
- ❌ `POST /api/call/text` - 텍스트 기반 통화 (제거됨)
- ✅ `POST /api/call/audio` - 음성 파일 기반 통화 (유지)

### 4. Message API (`/api/messages`)
- ✅ `GET /api/messages/session/{sessionId}` - 세션별 메시지 조회 (변경 없음)

### 5. Test API (`/api/test/groq`)
- ✅ `GET /api/test/groq/test` - Groq API 테스트 (변경 없음)
- ✅ `POST /api/test/groq/chat` - 채팅 모드 테스트 (변경 없음)
- ✅ `POST /api/test/groq/call` - 통화 모드 테스트 (변경 없음)

## 주요 변경사항

### ✅ 추가된 API
1. **CharacterController 전체 구현**
   - 이전: 비어있었음
   - 현재: 4개의 엔드포인트 모두 구현됨

### ❌ 제거된 API
1. **POST /api/call/text**
   - 이유: audio만 사용하기로 결정
   - 대체: POST /api/call/audio 사용

### 🔄 변경 없음
- Chat API
- Message API
- Test API

## API 상세 명세

### POST /api/characters
**요청:**
```json
{
  "mbti": "ENFJ",
  "attachment": "안정형",
  "teto": 75,
  "gender": "FEMALE",
  "name": "지은",
  "voiceType": "TYPE1",
  "description": "캐릭터 소개",
  "imageUrl": "https://example.com/image.jpg"
}
```

**응답:**
```json
{
  "characterId": 1,
  "mbti": "ENFJ",
  "attachment": "안정형",
  "teto": 75,
  "gender": "FEMALE",
  "name": "지은",
  "description": "캐릭터 소개",
  "imageUrl": "https://example.com/image.jpg",
  "voiceType": "TYPE1"
}
```

### GET /api/characters/{characterId}
**응답:** CharacterResponseDto

### GET /api/characters/recent
**응답:** CharacterResponseDto (없으면 null)

### POST /api/characters/{characterId}/select
**응답:**
```json
{
  "characterId": 1,
  "sessionId": 1,
  "character": { ... }
}
```

### POST /api/call/audio
**요청:** multipart/form-data
- audio: (파일)
- characterId: Long
- sessionId: Long (선택)

**응답:** audio/mpeg (MP3 파일)







