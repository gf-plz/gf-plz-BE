#!/bin/bash

echo "=== 애플리케이션 성능 로그 확인 스크립트 ==="
echo ""

# EC2 서버에서 실행할 스크립트
SERVER_LOG="/home/ubuntu/app.log"

if [ ! -f "$SERVER_LOG" ]; then
    echo "❌ 로그 파일을 찾을 수 없습니다: $SERVER_LOG"
    exit 1
fi

echo "1. 최근 에러 로그:"
echo "=================="
grep -i "error\|exception\|failed\|timeout" "$SERVER_LOG" | tail -20
echo ""

echo "2. 최근 API 요청 로그 (Spring Boot 기본 로그):"
echo "============================================="
tail -50 "$SERVER_LOG" | grep -E "GET|POST|PUT|DELETE|PATCH" | tail -20
echo ""

echo "3. Groq API 호출 관련 로그:"
echo "=========================="
grep -i "groq\|api.*call\|generateReply" "$SERVER_LOG" | tail -20
echo ""

echo "4. 데이터베이스 쿼리 로그 (show-sql이 활성화된 경우):"
echo "=================================================="
grep -i "hibernate\|sql\|query" "$SERVER_LOG" | tail -20
echo ""

echo "5. 최근 100줄 로그 (전체):"
echo "========================"
tail -100 "$SERVER_LOG"
echo ""

echo "6. 느린 응답 시간 확인 (타임스탬프 기반):"
echo "========================================"
# 타임스탬프가 있는 경우 시간 차이 계산
tail -200 "$SERVER_LOG" | grep -E "^\d{4}-\d{2}-\d{2}" | tail -30
echo ""

echo "=== 로그 확인 완료 ==="
echo ""
echo "추가 확인 사항:"
echo "- 실시간 로그 모니터링: tail -f $SERVER_LOG"
echo "- 특정 시간대 로그: grep '2025-11-27 14:' $SERVER_LOG"
echo "- 에러만 필터링: grep -i error $SERVER_LOG | tail -50"

