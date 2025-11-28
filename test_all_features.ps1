# 로컬 서버 테스트 스크립트 (PowerShell)
$BASE_URL = "http://localhost:8080"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "로컬 서버 기능 테스트 시작" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 테스트 결과 카운터
$script:PASSED = 0
$script:FAILED = 0

# 테스트 함수
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [string]$Data = $null,
        [int]$ExpectedStatus = 200
    )
    
    Write-Host -NoNewline "테스트: $Name ... "
    
    try {
        if ($Method -eq "GET") {
            $response = Invoke-WebRequest -Uri $Url -Method GET -UseBasicParsing -ErrorAction Stop
        } elseif ($Method -eq "POST") {
            $body = @{}
            if ($Data) {
                $body = $Data | ConvertFrom-Json | ConvertTo-Json -Compress
            }
            $response = Invoke-WebRequest -Uri $Url -Method POST `
                -ContentType "application/json; charset=UTF-8" `
                -Body $body `
                -UseBasicParsing -ErrorAction Stop
        }
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Host "✓ 통과" -ForegroundColor Green -NoNewline
            Write-Host " (HTTP $($response.StatusCode))"
            $script:PASSED++
            return $true
        } else {
            Write-Host "✗ 실패" -ForegroundColor Red -NoNewline
            Write-Host " (예상: HTTP $ExpectedStatus, 실제: HTTP $($response.StatusCode))"
            $script:FAILED++
            return $false
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host "✓ 통과" -ForegroundColor Green -NoNewline
            Write-Host " (HTTP $statusCode)"
            $script:PASSED++
            return $true
        } else {
            Write-Host "✗ 실패" -ForegroundColor Red -NoNewline
            Write-Host " (예상: HTTP $ExpectedStatus, 실제: HTTP $statusCode)"
            Write-Host "  오류: $($_.Exception.Message)" -ForegroundColor Yellow
            $script:FAILED++
            return $false
        }
    }
}

# 1. 서버 상태 확인
Write-Host "1. 서버 상태 확인" -ForegroundColor Yellow
Write-Host "-----------------" -ForegroundColor Yellow
Test-Endpoint -Name "서버 응답 확인" -Method "GET" -Url "$BASE_URL/api/characters"
Write-Host ""

# 2. Character API 테스트
Write-Host "2. Character API 테스트" -ForegroundColor Yellow
Write-Host "----------------------" -ForegroundColor Yellow

# 2.1 캐릭터 목록 조회
Test-Endpoint -Name "캐릭터 목록 조회" -Method "GET" -Url "$BASE_URL/api/characters"
try {
    $charactersResponse = Invoke-RestMethod -Uri "$BASE_URL/api/characters" -Method GET
    Write-Host "  응답: $($charactersResponse | ConvertTo-Json -Compress | Select-Object -First 100)..." -ForegroundColor Gray
} catch {
    Write-Host "  응답 확인 실패" -ForegroundColor Yellow
}
Write-Host ""

# 2.2 캐릭터 생성
$characterData = @{
    mbti = "ENFP"
    attachment = "안정형"
    teto = 50
    gender = "FEMALE"
    name = "테스트캐릭터"
    description = "테스트용 캐릭터입니다"
    imageUrl = "https://example.com/image.jpg"
    voiceType = "TYPE1"
} | ConvertTo-Json

$createResult = Test-Endpoint -Name "캐릭터 생성" -Method "POST" -Url "$BASE_URL/api/characters" -Data $characterData
if ($createResult) {
    try {
        $createResponse = Invoke-RestMethod -Uri "$BASE_URL/api/characters" -Method POST `
            -ContentType "application/json; charset=UTF-8" `
            -Body $characterData
        $CHARACTER_ID = $createResponse.characterId
        Write-Host "  생성된 캐릭터 ID: $CHARACTER_ID" -ForegroundColor Gray
    } catch {
        Write-Host "  캐릭터 ID 추출 실패" -ForegroundColor Yellow
        $CHARACTER_ID = $null
    }
} else {
    $CHARACTER_ID = $null
}
Write-Host ""

if ($CHARACTER_ID) {
    # 2.3 캐릭터 조회
    Test-Endpoint -Name "캐릭터 조회 (ID: $CHARACTER_ID)" -Method "GET" -Url "$BASE_URL/api/characters/$CHARACTER_ID"
    Write-Host ""
    
    # 2.4 캐릭터 선택
    $selectResult = Test-Endpoint -Name "캐릭터 선택 (ID: $CHARACTER_ID)" -Method "POST" -Url "$BASE_URL/api/characters/$CHARACTER_ID/select"
    if ($selectResult) {
        try {
            $selectResponse = Invoke-RestMethod -Uri "$BASE_URL/api/characters/$CHARACTER_ID/select" -Method POST
            $SESSION_ID = $selectResponse.sessionId
            Write-Host "  생성된 세션 ID: $SESSION_ID" -ForegroundColor Gray
        } catch {
            Write-Host "  세션 ID 추출 실패" -ForegroundColor Yellow
            $SESSION_ID = $null
        }
    } else {
        $SESSION_ID = $null
    }
    Write-Host ""
}

# 2.5 최근 캐릭터 조회
Test-Endpoint -Name "최근 캐릭터 조회" -Method "GET" -Url "$BASE_URL/api/characters/recent"
Write-Host ""

# 3. Chat API 테스트
Write-Host "3. Chat API 테스트" -ForegroundColor Yellow
Write-Host "------------------" -ForegroundColor Yellow
if ($CHARACTER_ID -and $SESSION_ID) {
    $chatData = @{
        characterId = $CHARACTER_ID
        sessionId = $SESSION_ID
        content = "안녕하세요!"
    } | ConvertTo-Json
    
    Test-Endpoint -Name "채팅 메시지 전송" -Method "POST" -Url "$BASE_URL/api/chat" -Data $chatData
    Write-Host ""
} else {
    Write-Host "  캐릭터 ID 또는 세션 ID가 없어 스킵합니다." -ForegroundColor Yellow
    Write-Host ""
}

# 4. Call API 테스트
Write-Host "4. Call API 테스트" -ForegroundColor Yellow
Write-Host "------------------" -ForegroundColor Yellow
Write-Host "  음성 파일 기반 통화는 실제 파일이 필요하므로 스킵합니다." -ForegroundColor Yellow
Write-Host "  엔드포인트: POST /api/call/audio" -ForegroundColor Gray
Write-Host ""

# 5. Message API 테스트
Write-Host "5. Message API 테스트" -ForegroundColor Yellow
Write-Host "---------------------" -ForegroundColor Yellow
if ($SESSION_ID) {
    Test-Endpoint -Name "세션 메시지 조회 (Session ID: $SESSION_ID)" -Method "GET" -Url "$BASE_URL/api/messages/session/$SESSION_ID"
    Write-Host ""
} else {
    Write-Host "  세션 ID가 없어 스킵합니다." -ForegroundColor Yellow
    Write-Host ""
}

# 6. Groq API 테스트
Write-Host "6. Groq API 테스트" -ForegroundColor Yellow
Write-Host "------------------" -ForegroundColor Yellow
Test-Endpoint -Name "Groq API 테스트" -Method "GET" -Url "$BASE_URL/api/test/groq/test"
Write-Host ""

# 결과 요약
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "테스트 결과 요약" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "통과: $script:PASSED" -ForegroundColor Green
Write-Host "실패: $script:FAILED" -ForegroundColor Red
Write-Host ""

if ($script:FAILED -eq 0) {
    Write-Host "모든 테스트가 통과했습니다!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "일부 테스트가 실패했습니다." -ForegroundColor Red
    exit 1
}



