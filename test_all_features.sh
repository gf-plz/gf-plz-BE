#!/bin/bash

# 로컬 서버 테스트 스크립트
BASE_URL="http://localhost:8080"

echo "=========================================="
echo "로컬 서버 기능 테스트 시작"
echo "=========================================="
echo ""

# 색상 코드
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 테스트 결과 카운터
PASSED=0
FAILED=0

# 테스트 함수
test_endpoint() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_status=$5
    
    echo -n "테스트: $name ... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$url" 2>&1)
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$url" \
            -H "Content-Type: application/json" \
            -d "$data" 2>&1)
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ 통과${NC} (HTTP $http_code)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ 실패${NC} (예상: HTTP $expected_status, 실제: HTTP $http_code)"
        echo "  응답: $body" | head -c 200
        echo ""
        ((FAILED++))
        return 1
    fi
}

# 1. 서버 상태 확인
echo "1. 서버 상태 확인"
echo "-----------------"
test_endpoint "서버 응답 확인" "GET" "$BASE_URL/api/characters" "" "200"
echo ""

# 2. Character API 테스트
echo "2. Character API 테스트"
echo "----------------------"

# 2.1 캐릭터 목록 조회
test_endpoint "캐릭터 목록 조회" "GET" "$BASE_URL/api/characters" "" "200"
CHARACTERS_RESPONSE=$(curl -s "$BASE_URL/api/characters")
echo "  응답: $(echo $CHARACTERS_RESPONSE | head -c 100)..."
echo ""

# 2.2 캐릭터 생성
CHARACTER_DATA='{
  "mbti": "ENFP",
  "attachment": "안정형",
  "teto": 50,
  "gender": "FEMALE",
  "name": "테스트캐릭터",
  "description": "테스트용 캐릭터입니다",
  "imageUrl": "https://example.com/image.jpg",
  "voiceType": "TYPE1"
}'

test_endpoint "캐릭터 생성" "POST" "$BASE_URL/api/characters" "$CHARACTER_DATA" "200"
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/characters" \
    -H "Content-Type: application/json" \
    -d "$CHARACTER_DATA")

# 생성된 캐릭터 ID 추출
CHARACTER_ID=$(echo $CREATE_RESPONSE | grep -o '"characterId":[0-9]*' | grep -o '[0-9]*' | head -1)
echo "  생성된 캐릭터 ID: $CHARACTER_ID"
echo ""

if [ -n "$CHARACTER_ID" ]; then
    # 2.3 캐릭터 조회
    test_endpoint "캐릭터 조회 (ID: $CHARACTER_ID)" "GET" "$BASE_URL/api/characters/$CHARACTER_ID" "" "200"
    echo ""
    
    # 2.4 캐릭터 선택
    test_endpoint "캐릭터 선택 (ID: $CHARACTER_ID)" "POST" "$BASE_URL/api/characters/$CHARACTER_ID/select" "" "200"
    SELECT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/characters/$CHARACTER_ID/select")
    SESSION_ID=$(echo $SELECT_RESPONSE | grep -o '"sessionId":[0-9]*' | grep -o '[0-9]*' | head -1)
    echo "  생성된 세션 ID: $SESSION_ID"
    echo ""
fi

# 2.5 최근 캐릭터 조회
test_endpoint "최근 캐릭터 조회" "GET" "$BASE_URL/api/characters/recent" "" "200"
echo ""

# 3. Chat API 테스트
echo "3. Chat API 테스트"
echo "------------------"
if [ -n "$CHARACTER_ID" ] && [ -n "$SESSION_ID" ]; then
    CHAT_DATA="{
      \"characterId\": $CHARACTER_ID,
      \"sessionId\": $SESSION_ID,
      \"content\": \"안녕하세요!\"
    }"
    test_endpoint "채팅 메시지 전송" "POST" "$BASE_URL/api/chat" "$CHAT_DATA" "200"
    echo ""
fi

# 4. Call API 테스트 (음성 파일은 스킵, 실제 파일이 필요함)
echo "4. Call API 테스트"
echo "------------------"
echo "  음성 파일 기반 통화는 실제 파일이 필요하므로 스킵합니다."
echo "  엔드포인트: POST /api/call/audio"
echo ""

# 5. Message API 테스트
echo "5. Message API 테스트"
echo "---------------------"
if [ -n "$SESSION_ID" ]; then
    test_endpoint "세션 메시지 조회 (Session ID: $SESSION_ID)" "GET" "$BASE_URL/api/messages/session/$SESSION_ID" "" "200"
    echo ""
fi

# 6. History API 테스트
echo "6. History API 테스트"
echo "---------------------"
if [ -n "$CHARACTER_ID" ]; then
    test_endpoint "관계 히스토리 조회 (Character ID: $CHARACTER_ID)" "GET" "$BASE_URL/api/history?characterId=$CHARACTER_ID" "" "200"
    echo ""
fi

# 7. Groq API 테스트
echo "7. Groq API 테스트"
echo "------------------"
test_endpoint "Groq API 테스트" "GET" "$BASE_URL/api/test/groq/test" "" "200"
echo ""

# 결과 요약
echo "=========================================="
echo "테스트 결과 요약"
echo "=========================================="
echo -e "${GREEN}통과: $PASSED${NC}"
echo -e "${RED}실패: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}모든 테스트가 통과했습니다!${NC}"
    exit 0
else
    echo -e "${RED}일부 테스트가 실패했습니다.${NC}"
    exit 1
fi

