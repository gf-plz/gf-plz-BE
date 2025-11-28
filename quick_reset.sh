#!/bin/bash
# EC2 서버에서 빠르게 실행할 수 있는 데이터베이스 재생성 스크립트

echo "=========================================="
echo "데이터베이스 재생성 스크립트"
echo "=========================================="
echo ""

# 1. 애플리케이션 중지
echo "[1/4] 애플리케이션 중지 중..."
PID=$(pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar")
if [ -n "$PID" ]; then
    echo "   프로세스 ID: $PID 종료 중..."
    kill -15 $PID
    sleep 5
    
    # 강제 종료
    PID=$(pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar")
    if [ -n "$PID" ]; then
        echo "   강제 종료 중..."
        kill -9 $PID
        sleep 2
    fi
    echo "   ✅ 애플리케이션 종료 완료"
else
    echo "   ℹ️  실행 중인 애플리케이션이 없습니다."
fi

# 2. 데이터베이스 파일 삭제
echo ""
echo "[2/4] 데이터베이스 파일 삭제 중..."
DB_PATH="/home/ubuntu/data/gfplz"

if [ -f "${DB_PATH}.mv.db" ]; then
    rm "${DB_PATH}.mv.db"
    echo "   ✅ ${DB_PATH}.mv.db 삭제 완료"
else
    echo "   ℹ️  ${DB_PATH}.mv.db 파일이 없습니다."
fi

if [ -f "${DB_PATH}.trace.db" ]; then
    rm "${DB_PATH}.trace.db"
    echo "   ✅ ${DB_PATH}.trace.db 삭제 완료"
else
    echo "   ℹ️  ${DB_PATH}.trace.db 파일이 없습니다."
fi

if [ -f "${DB_PATH}.lock.db" ]; then
    rm "${DB_PATH}.lock.db"
    echo "   ✅ ${DB_PATH}.lock.db 삭제 완료"
else
    echo "   ℹ️  ${DB_PATH}.lock.db 파일이 없습니다."
fi

# 3. 서버 재시작
echo ""
echo "[3/4] 서버 재시작 중..."
cd /home/ubuntu
nohup java -jar -Dspring.profiles.active=prod \
  -DDB_PATH=/home/ubuntu/data/gfplz \
  gf-plz-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

NEW_PID=$!
echo "   새 프로세스 시작됨 (PID: $NEW_PID)"
echo "   서버 시작 대기 중 (10초)..."
sleep 10

# 4. 서버 시작 확인
echo ""
echo "[4/4] 서버 상태 확인 중..."
if pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar" > /dev/null; then
    echo "   ✅ 서버가 정상적으로 실행 중입니다!"
    echo ""
    echo "=========================================="
    echo "✅ 데이터베이스 재생성 완료!"
    echo "=========================================="
    echo ""
    echo "최근 로그 (마지막 20줄):"
    echo "----------------------------------------"
    tail -20 app.log
    echo ""
    echo "전체 로그 확인: tail -f /home/ubuntu/app.log"
else
    echo "   ❌ 서버 시작 실패!"
    echo ""
    echo "=========================================="
    echo "❌ 오류 발생!"
    echo "=========================================="
    echo ""
    echo "로그 확인 (마지막 50줄):"
    echo "----------------------------------------"
    tail -50 app.log
    exit 1
fi




