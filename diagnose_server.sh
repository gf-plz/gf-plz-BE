#!/bin/bash

echo "=== EC2 서버 진단 스크립트 ==="
echo ""

# 1. 프로세스 확인
echo "1. Java 프로세스 확인:"
ps aux | grep "gf-plz-0.0.1-SNAPSHOT.jar" | grep -v grep || echo "  ❌ 애플리케이션이 실행 중이지 않습니다"
echo ""

# 2. 포트 확인
echo "2. 포트 8080 상태 확인:"
if command -v netstat &> /dev/null; then
    netstat -tlnp 2>/dev/null | grep ":8080 " || echo "  ❌ 포트 8080이 열려있지 않습니다"
elif command -v ss &> /dev/null; then
    ss -tlnp 2>/dev/null | grep ":8080 " || echo "  ❌ 포트 8080이 열려있지 않습니다"
else
    echo "  ⚠️ netstat 또는 ss 명령어를 사용할 수 없습니다"
fi
echo ""

# 3. 필수 파일 확인
echo "3. 필수 파일 확인:"
cd /home/ubuntu

if [ -f "gf-plz-0.0.1-SNAPSHOT.jar" ]; then
    echo "  ✅ JAR 파일 존재: $(ls -lh gf-plz-0.0.1-SNAPSHOT.jar | awk '{print $5}')"
else
    echo "  ❌ JAR 파일이 없습니다!"
fi

if [ -f "google-credentials.json" ]; then
    echo "  ✅ Google credentials 파일 존재"
else
    echo "  ❌ google-credentials.json 파일이 없습니다!"
fi

if [ -f ".env" ]; then
    echo "  ✅ .env 파일 존재"
    echo "  .env 내용 (API 키는 마스킹):"
    sed 's/\(GROQ_API_KEY=\).*/\1***/' .env
else
    echo "  ❌ .env 파일이 없습니다!"
fi
echo ""

# 4. 데이터베이스 디렉터리 확인
echo "4. 데이터베이스 디렉터리 확인:"
if [ -d "/home/ubuntu/data/gfplz" ]; then
    echo "  ✅ 데이터베이스 디렉터리 존재"
    ls -lh /home/ubuntu/data/gfplz/ 2>/dev/null | head -5
else
    echo "  ⚠️ 데이터베이스 디렉터리가 없습니다 (첫 실행 시 정상)"
fi
echo ""

# 5. 로그 확인
echo "5. 최근 애플리케이션 로그 (마지막 50줄):"
if [ -f "app.log" ]; then
    echo "  === 로그 시작 ==="
    tail -50 app.log
    echo "  === 로그 끝 ==="
    
    # 에러 확인
    echo ""
    echo "  에러 로그 확인:"
    if grep -i "error\|exception\|failed" app.log | tail -10; then
        echo ""
    else
        echo "  ✅ 최근 에러 없음"
    fi
else
    echo "  ❌ app.log 파일이 없습니다!"
fi
echo ""

# 6. 시스템 리소스 확인
echo "6. 시스템 리소스 확인:"
echo "  메모리 사용량:"
free -h | head -2
echo ""
echo "  디스크 사용량:"
df -h / | tail -1
echo ""

# 7. 환경 변수 확인
echo "7. 환경 변수 확인:"
echo "  GOOGLE_APPLICATION_CREDENTIALS: ${GOOGLE_APPLICATION_CREDENTIALS:-❌ 설정되지 않음}"
echo "  DB_PATH: ${DB_PATH:-❌ 설정되지 않음}"
echo ""

# 8. HTTP 응답 확인
echo "8. HTTP 응답 확인:"
if command -v curl &> /dev/null; then
    if curl -s -f http://localhost:8080/api/characters > /dev/null 2>&1; then
        echo "  ✅ 서버가 HTTP 요청에 응답합니다"
    else
        echo "  ❌ 서버가 HTTP 요청에 응답하지 않습니다"
        echo "  응답 내용:"
        curl -s http://localhost:8080/api/characters 2>&1 | head -5
    fi
else
    echo "  ⚠️ curl 명령어를 사용할 수 없습니다"
fi
echo ""

echo "=== 진단 완료 ==="
echo ""
echo "문제 해결 방법:"
echo "1. 애플리케이션이 실행 중이 아니면:"
echo "   cd /home/ubuntu"
echo "   export GOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json"
echo "   nohup java -jar -Dspring.profiles.active=prod -DDB_PATH=/home/ubuntu/data/gfplz -DGOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json gf-plz-0.0.1-SNAPSHOT.jar > app.log 2>&1 &"
echo ""
echo "2. 로그에서 에러를 확인하고 문제를 해결하세요"
echo "3. 파일이 없으면 GitHub Actions 배포를 다시 실행하세요"





