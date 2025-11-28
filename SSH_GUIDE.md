# EC2 서버 SSH 접속 가이드

## 서버 정보

- **IP 주소**: `3.27.10.133`
- **사용자명**: `ubuntu`
- **포트**: `22` (기본 SSH 포트)

## SSH 접속 방법

### 방법 1: SSH 키 파일 사용 (권장)

```bash
# SSH 키 파일이 있는 경우
ssh -i /path/to/your-key.pem ubuntu@3.27.10.133

# 예시 (Windows)
ssh -i C:\Users\YourName\.ssh\ec2-key.pem ubuntu@3.27.10.133

# 예시 (Mac/Linux)
ssh -i ~/.ssh/ec2-key.pem ubuntu@3.27.10.133
```

### 방법 2: SSH 키 없이 접속 (비밀번호 사용)

```bash
# 비밀번호로 접속
ssh ubuntu@3.27.10.133
```

### 방법 3: SSH 키를 SSH config에 등록

SSH config 파일에 등록하면 키 파일 경로를 매번 입력하지 않아도 됩니다:

**Windows**: `C:\Users\YourName\.ssh\config`  
**Mac/Linux**: `~/.ssh/config`

```bash
# SSH config 파일 편집
nano ~/.ssh/config  # 또는 notepad C:\Users\YourName\.ssh\config

# 다음 내용 추가
Host gf-plz-ec2
    HostName 3.27.10.133
    User ubuntu
    IdentityFile ~/.ssh/ec2-key.pem
    StrictHostKeyChecking no

# 저장 후 간단하게 접속
ssh gf-plz-ec2
```

## 접속 후 확인 사항

### 1. 서버 상태 확인

```bash
# 현재 디렉터리 확인
pwd
# 출력: /home/ubuntu

# 애플리케이션 프로세스 확인
ps aux | grep "gf-plz-0.0.1-SNAPSHOT.jar"

# 로그 확인
tail -50 /home/ubuntu/app.log
```

### 2. 파일 확인

```bash
# 필수 파일 확인
ls -la /home/ubuntu/

# 확인해야 할 파일들:
# - gf-plz-0.0.1-SNAPSHOT.jar (애플리케이션 JAR)
# - google-credentials.json (Google Cloud 인증 정보)
# - .env (환경 변수)
# - app.log (애플리케이션 로그)
```

## 접속 문제 해결

### 문제 1: "Permission denied (publickey)" 오류

**원인**: SSH 키 파일 권한 문제 또는 키 파일 경로 오류

**해결 방법**:

```bash
# Mac/Linux에서 키 파일 권한 수정
chmod 600 /path/to/your-key.pem

# Windows에서는 파일 속성에서 권한 확인
# 키 파일을 우클릭 → 속성 → 보안 → 권한 확인
```

### 문제 2: "Connection timed out" 오류

**원인**: 
- EC2 인스턴스가 중지됨
- 보안 그룹에서 SSH 포트(22)가 차단됨
- 네트워크 문제

**해결 방법**:

1. **AWS 콘솔에서 인스턴스 상태 확인**
   - EC2 Dashboard → Instances
   - 인스턴스 상태가 "running"인지 확인
   - 인스턴스가 중지되어 있으면 "Start instance" 클릭

2. **보안 그룹 확인**
   - EC2 Dashboard → Security Groups
   - 인바운드 규칙에서 SSH(포트 22) 허용 확인
   - 필요시 규칙 추가:
     - Type: SSH
     - Protocol: TCP
     - Port: 22
     - Source: My IP (또는 0.0.0.0/0 - 보안상 권장하지 않음)

### 문제 3: "Host key verification failed" 오류

**원인**: 호스트 키가 변경되었거나 처음 접속하는 경우

**해결 방법**:

```bash
# known_hosts에서 해당 호스트 제거
ssh-keygen -R 3.27.10.133

# 또는 StrictHostKeyChecking 옵션 사용
ssh -o StrictHostKeyChecking=no ubuntu@3.27.10.133
```

## 자주 사용하는 명령어

### 로그 확인

```bash
# 실시간 로그 확인
tail -f /home/ubuntu/app.log

# 최근 100줄 확인
tail -100 /home/ubuntu/app.log

# 에러만 확인
grep -i "error\|exception" /home/ubuntu/app.log | tail -50
```

### 애플리케이션 재시작

```bash
# 기존 프로세스 종료
pkill -f "gf-plz-0.0.1-SNAPSHOT.jar"

# 애플리케이션 재시작
cd /home/ubuntu
export GOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json
nohup java -jar -Dspring.profiles.active=prod \
  -DDB_PATH=/home/ubuntu/data/gfplz \
  -DGOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json \
  gf-plz-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 확인
sleep 5
tail -30 app.log
```

### 파일 업로드/다운로드

```bash
# 로컬에서 서버로 파일 업로드
scp -i /path/to/key.pem local-file.txt ubuntu@3.27.10.133:/home/ubuntu/

# 서버에서 로컬로 파일 다운로드
scp -i /path/to/key.pem ubuntu@3.27.10.133:/home/ubuntu/app.log ./
```

## Windows에서 SSH 사용하기

### 방법 1: PowerShell 또는 CMD

```powershell
# PowerShell에서
ssh -i C:\path\to\key.pem ubuntu@3.27.10.133

# CMD에서
ssh -i C:\path\to\key.pem ubuntu@3.27.10.133
```

### 방법 2: Git Bash

```bash
# Git Bash에서
ssh -i /c/path/to/key.pem ubuntu@3.27.10.133
```

### 방법 3: WSL (Windows Subsystem for Linux)

```bash
# WSL에서
ssh -i ~/.ssh/ec2-key.pem ubuntu@3.27.10.133
```

### 방법 4: PuTTY (Windows 전용)

1. PuTTY 다운로드 및 설치
2. PuTTY 실행
3. Host Name: `ubuntu@3.27.10.133`
4. Port: `22`
5. Connection → SSH → Auth → Credentials
   - Private key file for authentication: `.pem` 파일 선택
6. Open 클릭

## Mac/Linux에서 SSH 사용하기

```bash
# 기본 명령어
ssh -i ~/.ssh/ec2-key.pem ubuntu@3.27.10.133

# 키 파일 권한 설정 (처음 한 번만)
chmod 600 ~/.ssh/ec2-key.pem

# SSH config에 등록 후
ssh gf-plz-ec2
```

## 보안 주의사항

1. **SSH 키 파일 보관**
   - 키 파일을 안전한 곳에 보관
   - 공개 저장소에 업로드하지 않기
   - `.gitignore`에 키 파일 추가 확인

2. **비밀번호 인증 비활성화** (권장)
   - SSH 키만 사용하도록 설정
   - 비밀번호 인증은 보안상 취약

3. **IP 제한**
   - 보안 그룹에서 특정 IP만 SSH 접속 허용
   - `0.0.0.0/0`은 모든 IP 허용 (위험)

## 빠른 참조

```bash
# 접속
ssh ubuntu@3.27.10.133

# 로그 확인
tail -f /home/ubuntu/app.log

# 프로세스 확인
ps aux | grep java

# 애플리케이션 재시작
cd /home/ubuntu && pkill -f "gf-plz" && nohup java -jar -Dspring.profiles.active=prod -DDB_PATH=/home/ubuntu/data/gfplz -DGOOGLE_APPLICATION_CREDENTIALS=/home/ubuntu/google-credentials.json gf-plz-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```



