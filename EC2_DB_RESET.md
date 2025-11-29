# EC2 서버 데이터베이스 재생성 가이드

## 방법 1: 스크립트 사용 (권장)

### 1. 스크립트를 EC2 서버에 업로드

로컬에서 실행:
```bash
scp -i your-key.pem reset_db.sh ubuntu@3.27.10.133:/home/ubuntu/
```

### 2. EC2 서버에 SSH 접속

```bash
ssh -i your-key.pem ubuntu@3.27.10.133
```

### 3. 스크립트 실행

```bash
chmod +x reset_db.sh
./reset_db.sh
```

## 방법 2: 직접 명령어 실행

EC2 서버에 SSH 접속 후 다음 명령어를 순서대로 실행:

```bash
# 1. 애플리케이션 중지
PID=$(pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar")
if [ -n "$PID" ]; then
    echo "프로세스 종료 중: $PID"
    kill -15 $PID
    sleep 5
    
    # 강제 종료
    PID=$(pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar")
    if [ -n "$PID" ]; then
        kill -9 $PID
    fi
fi

# 2. 데이터베이스 파일 삭제
rm -f /home/ubuntu/data/gfplz.mv.db
rm -f /home/ubuntu/data/gfplz.trace.db
rm -f /home/ubuntu/data/gfplz.lock.db

echo "데이터베이스 파일 삭제 완료"

# 3. 서버 재시작
cd /home/ubuntu
nohup java -jar -Dspring.profiles.active=prod \
  -DDB_PATH=/home/ubuntu/data/gfplz \
  gf-plz-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

echo "서버 재시작 완료. PID: $!"
echo "로그 확인: tail -f app.log"
```

## 확인

서버 재시작 후 다음 명령어로 확인:

```bash
# 프로세스 확인
pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar"

# 로그 확인
tail -f /home/ubuntu/app.log

# API 테스트
curl http://localhost:8080/api/characters
```

## 주의사항

- 데이터베이스 파일을 삭제하면 **모든 데이터가 삭제**됩니다.
- 서버 재시작 시 `ddl-auto: update` 설정으로 엔티티에 맞춰 테이블이 자동 생성됩니다.
- ERD에 맞춰 `Status` 테이블에는 `애착타입` 컬럼이 없고, `Character` 테이블에만 있습니다.





