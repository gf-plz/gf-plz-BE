# 로그 확인 가이드

## EC2 서버에서 로그 확인

### 1. 기본 로그 확인

```bash
# SSH 접속
ssh ubuntu@3.27.10.133

# 실시간 로그 확인 (가장 유용!)
tail -f /home/ubuntu/app.log

# 최근 100줄 확인
tail -100 /home/ubuntu/app.log

# 최근 50줄 확인
tail -50 /home/ubuntu/app.log
```

### 2. 성능 관련 로그 확인

```bash
# 에러만 확인
grep -i "error\|exception\|failed" /home/ubuntu/app.log | tail -50

# 특정 API 엔드포인트 확인
grep "/api/call" /home/ubuntu/app.log | tail -30

# Groq API 호출 확인
grep -i "groq\|api.*call" /home/ubuntu/app.log | tail -30

# 느린 응답 확인 (타임스탬프 기반)
tail -200 /home/ubuntu/app.log | grep -E "^\d{4}-\d{2}-\d{2}"
```

### 3. 성능 스크립트 사용

```bash
# 성능 확인 스크립트 실행
chmod +x check_performance.sh
./check_performance.sh
```

### 4. 특정 시간대 로그 확인

```bash
# 오늘 14시대 로그
grep "2025-11-27 14:" /home/ubuntu/app.log

# 최근 1시간 로그 (현재 시간 기준)
tail -1000 /home/ubuntu/app.log | grep "$(date +%Y-%m-%d\ %H)"
```

## 로컬에서 로그 확인

### 로컬 개발 환경

```bash
# 로컬에서는 콘솔에 직접 출력됨
# 또는 IDE의 콘솔 창에서 확인

# 로그 파일이 있다면
tail -f logs/application.log
```

## 로그에서 확인할 사항

### 1. 응답 시간이 느린 경우

다음 항목을 확인하세요:

- **Groq API 호출 시간**: `groqClient.generateReply` 호출 전후 시간
- **데이터베이스 쿼리 시간**: SQL 실행 시간
- **TTS 생성 시간**: Google Cloud TTS 호출 시간
- **네트워크 지연**: 외부 API 호출 시간

### 2. 일반적인 성능 문제 원인

1. **Groq API 응답 지연**
   - Groq API가 느릴 수 있음
   - 네트워크 문제
   - API 키 문제

2. **데이터베이스 쿼리 느림**
   - 인덱스 부족
   - 대량 데이터 조회
   - N+1 쿼리 문제

3. **TTS 생성 시간**
   - Google Cloud TTS API 호출 시간
   - 음성 파일 생성 시간

### 3. 로그 레벨 조정

성능 문제 진단을 위해 로그 레벨을 조정할 수 있습니다:

```yaml
# application-prod.yml에서
logging:
  level:
    org.hibernate.SQL: DEBUG  # SQL 쿼리 로그 활성화
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # 파라미터 바인딩 로그
```

## 실시간 모니터링

```bash
# 실시간 로그 모니터링 (가장 추천!)
tail -f /home/ubuntu/app.log

# 특정 키워드만 실시간 모니터링
tail -f /home/ubuntu/app.log | grep -i "error\|slow\|timeout"

# 여러 터미널에서 동시에 모니터링
# 터미널 1: 전체 로그
tail -f /home/ubuntu/app.log

# 터미널 2: 에러만
tail -f /home/ubuntu/app.log | grep -i error

# 터미널 3: API 호출만
tail -f /home/ubuntu/app.log | grep -E "GET|POST|PUT|DELETE"
```

## 로그 파일 관리

```bash
# 로그 파일 크기 확인
ls -lh /home/ubuntu/app.log

# 로그 파일 압축 (오래된 로그)
gzip /home/ubuntu/app.log.1

# 로그 파일 삭제 (주의!)
# rm /home/ubuntu/app.log  # 서버 재시작 시 자동 생성됨
```

## 문제 해결 예시

### 예시 1: 특정 API가 느린 경우

```bash
# 해당 API 호출 로그만 확인
grep "/api/call/text" /home/ubuntu/app.log | tail -50

# 시간대별로 확인
grep "/api/call/text" /home/ubuntu/app.log | grep "2025-11-27 14:"
```

### 예시 2: Groq API가 느린 경우

```bash
# Groq 관련 로그 확인
grep -i "groq\|generateReply" /home/ubuntu/app.log | tail -50

# 에러 확인
grep -i "groq.*error\|groq.*exception" /home/ubuntu/app.log
```

### 예시 3: 데이터베이스가 느린 경우

```bash
# SQL 로그 활성화 후 (application-prod.yml 수정 필요)
grep "Hibernate:" /home/ubuntu/app.log | tail -50

# 느린 쿼리 확인
grep "took.*ms" /home/ubuntu/app.log
```





