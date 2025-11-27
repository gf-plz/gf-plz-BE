# EC2 서버 복구 가이드

## 서버 다운 상황 대응

### 1. EC2 인스턴스 상태 확인

AWS 콘솔에서 확인:
- EC2 Dashboard → Instances
- 인스턴스 상태 확인:
  - `running`: 정상 실행 중
  - `stopped`: 중지됨
  - `stopping`: 중지 중
  - `pending`: 시작 중

### 2. 서버 재시작 방법

#### 방법 1: AWS 콘솔에서 재시작 (권장)

1. AWS 콘솔 → EC2 → Instances
2. 인스턴스 선택 (3.27.10.133)
3. **Instance State** → **Start instance** (중지된 경우)
   또는 **Reboot instance** (실행 중이지만 응답 없는 경우)

#### 방법 2: SSH로 접속 시도

```bash
# 서버 접속 시도
ssh ubuntu@3.27.10.133

# 접속이 안 되면 서버가 완전히 다운된 상태
```

### 3. 서버 상태 진단

서버 문제를 진단하기 위해 진단 스크립트를 사용하세요:

```bash
# SSH 접속
ssh ubuntu@3.27.10.133

# 진단 스크립트 실행 (로컬에서 업로드 후)
chmod +x diagnose_server.sh
./diagnose_server.sh
```

또는 수동으로 확인:

```bash
# 1. 프로세스 확인
ps aux | grep "gf-plz-0.0.1-SNAPSHOT.jar"

# 2. 포트 확인
sudo netstat -tlnp | grep 8080

# 3. 로그 확인
tail -100 /home/ubuntu/app.log

# 4. 파일 확인
ls -la /home/ubuntu/gf-plz-0.0.1-SNAPSHOT.jar
ls -la /home/ubuntu/google-credentials.json
ls -la /home/ubuntu/.env
```

### 4. 서버 재시작 후 애플리케이션 실행

서버가 재시작되면 애플리케이션을 다시 실행해야 합니다:

```bash
# SSH 접속
ssh ubuntu@3.27.10.133

# 기존 프로세스 종료
PID=$(pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar")
if [ -n "$PID" ]; then
    kill -15 $PID
    sleep 5
    PID=$(pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar")
    if [ -n "$PID" ]; then
        kill -9 $PID
    fi
fi

# 애플리케이션 실행
cd /home/ubuntu
export GOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json
nohup java -jar -Dspring.profiles.active=prod \
  -DDB_PATH=/home/ubuntu/data/gfplz \
  -DGOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json \
  gf-plz-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 실행 확인
sleep 10
pgrep -f "gf-plz-0.0.1-SNAPSHOT.jar"
tail -30 app.log
```

### 5. 데이터베이스 재생성 (필요한 경우)

서버 재시작 후 데이터베이스도 재생성이 필요하면:

```bash
# 애플리케이션 중지
pkill -f "gf-plz-0.0.1-SNAPSHOT.jar"

# 데이터베이스 파일 삭제
rm -f /home/ubuntu/data/gfplz.mv.db
rm -f /home/ubuntu/data/gfplz.trace.db
rm -f /home/ubuntu/data/gfplz.lock.db

# 서버 재시작
cd /home/ubuntu
export GOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json
nohup java -jar -Dspring.profiles.active=prod \
  -DDB_PATH=/home/ubuntu/data/gfplz \
  -DGOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json \
  gf-plz-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

### 6. 자동 재시작 설정 (선택사항)

서버가 자동으로 재시작되도록 systemd 서비스로 등록:

```bash
# 서비스 파일 생성
sudo nano /etc/systemd/system/gf-plz.service
```

서비스 파일 내용:
```ini
[Unit]
Description=GF-PLZ Spring Boot Application
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu
Environment="GOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json"
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod -DDB_PATH=/home/ubuntu/data/gfplz -DGOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json /home/ubuntu/gf-plz-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=append:/home/ubuntu/app.log
StandardError=append:/home/ubuntu/app.log

[Install]
WantedBy=multi-user.target
```

서비스 활성화:
```bash
sudo systemctl daemon-reload
sudo systemctl enable gf-plz
sudo systemctl start gf-plz
sudo systemctl status gf-plz
```

## 문제 해결

### 서버가 계속 다운되는 경우

1. **메모리 부족 확인**
```bash
free -h
df -h
```

2. **로그 확인**
```bash
tail -100 /home/ubuntu/app.log
journalctl -u gf-plz -n 100
```

3. **포트 충돌 확인**
```bash
sudo netstat -tlnp | grep 8080
```

4. **Java 프로세스 확인**
```bash
ps aux | grep java
```

## 긴급 복구

서버가 완전히 응답하지 않는 경우:

1. AWS 콘솔에서 인스턴스 **Stop** → **Start**
2. 인스턴스가 시작되면 SSH 접속
3. 애플리케이션 재실행
4. 데이터베이스 재생성 (필요시)

