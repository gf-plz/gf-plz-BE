#!/bin/bash
# 데이터베이스 재생성 스크립트
# 사용법: ./reset_db.sh

echo "데이터베이스 재생성 스크립트"
echo "================================"

# Spring Boot 애플리케이션 중지
echo "1. Spring Boot 애플리케이션 중지 중..."
PID=$(pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar")
if [ -n "$PID" ]; then
    echo "   프로세스 ID: $PID 종료 중..."
    kill -15 $PID
    sleep 5
    
    # 여전히 실행 중이면 강제 종료
    PID=$(pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar")
    if [ -n "$PID" ]; then
        echo "   강제 종료 중..."
        kill -9 $PID
    fi
    echo "   애플리케이션 종료 완료"
else
    echo "   실행 중인 애플리케이션이 없습니다."
fi

# 데이터베이스 파일 삭제
echo ""
echo "2. 데이터베이스 파일 삭제 중..."
DB_PATH="${DB_PATH:-/home/ubuntu/data/gfplz}"

if [ -f "${DB_PATH}.mv.db" ]; then
    rm "${DB_PATH}.mv.db"
    echo "   ${DB_PATH}.mv.db 삭제 완료"
else
    echo "   ${DB_PATH}.mv.db 파일이 없습니다."
fi

if [ -f "${DB_PATH}.trace.db" ]; then
    rm "${DB_PATH}.trace.db"
    echo "   ${DB_PATH}.trace.db 삭제 완료"
else
    echo "   ${DB_PATH}.trace.db 파일이 없습니다."
fi

if [ -f "${DB_PATH}.lock.db" ]; then
    rm "${DB_PATH}.lock.db"
    echo "   ${DB_PATH}.lock.db 삭제 완료"
else
    echo "   ${DB_PATH}.lock.db 파일이 없습니다."
fi

echo ""
echo "3. 데이터베이스 재생성 준비 완료!"
echo "   서버를 재시작하면 엔티티에 맞춰 테이블이 자동 생성됩니다."
echo ""
echo "4. 서버 재시작 중..."
cd /home/ubuntu
nohup java -jar -Dspring.profiles.active=prod \
  -DDB_PATH=/home/ubuntu/data/gfplz \
  gf-plz-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

NEW_PID=$!
echo "   새 프로세스 시작됨 (PID: $NEW_PID)"
echo "   서버 시작 대기 중..."
sleep 10

# 서버 시작 확인
if pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar" > /dev/null; then
    echo ""
    echo "✅ 서버 재시작 완료!"
    echo "   최근 로그:"
    tail -20 app.log
else
    echo ""
    echo "❌ 서버 시작 실패!"
    echo "   로그 확인:"
    tail -50 app.log
    exit 1
fi

