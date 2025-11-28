package com.aigf.gf_plz.global.tts;

import com.aigf.gf_plz.global.tts.exception.TtsException;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechRequest;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Google Cloud TTS 클라이언트 구현체
 * 한국어를 지원하는 TTS 서비스를 제공합니다.
 */
@Service
@Primary
public class GoogleCloudTtsClient implements TtsClient {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudTtsClient.class);

    @Value("${google.cloud.tts.credentials-path:}")
    private String credentialsPath;

    private TextToSpeechClient textToSpeechClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * TextToSpeechClient를 초기화합니다.
     */
    private void initializeClient() {
        if (textToSpeechClient == null) {
            try {
                TextToSpeechSettings.Builder settingsBuilder = TextToSpeechSettings.newBuilder();
                
                // 서비스 계정 키 파일이 지정된 경우 직접 credentials 로드
                String finalCredentialsPath = null;
                
                String jsonContentDirect = null;
                
                if (credentialsPath != null && !credentialsPath.isBlank()) {
                    // credentialsPath가 JSON 내용인지 파일 경로인지 확인
                    if (credentialsPath.trim().startsWith("{")) {
                        // JSON 내용으로 직접 제공된 경우
                        jsonContentDirect = credentialsPath;
                        logger.info("Google Cloud credentials가 JSON 내용으로 직접 제공됨");
                    } else {
                        // 파일 경로로 제공된 경우
                        File credentialsFile = new File(credentialsPath);
                        if (!credentialsFile.exists()) {
                            logger.error("Google Cloud credentials 파일을 찾을 수 없습니다: {}", credentialsPath);
                            throw new TtsException(
                                "Google Cloud TTS 인증 파일을 찾을 수 없습니다: " + credentialsPath + 
                                ". 파일 경로를 확인하거나 환경 변수 GOOGLE_APPLICATION_CREDENTIALS를 설정해주세요."
                            );
                        }
                        // 파일 읽기 권한 확인
                        if (!credentialsFile.canRead()) {
                            logger.error("Google Cloud credentials 파일을 읽을 수 없습니다 (권한 없음): {}", credentialsPath);
                            throw new TtsException(
                                "Google Cloud TTS 인증 파일을 읽을 수 없습니다 (권한 없음): " + credentialsPath
                            );
                        }
                        finalCredentialsPath = credentialsFile.getAbsolutePath();
                        logger.info("Google Cloud credentials 파일 로드 중: {} (크기: {} bytes)", 
                            finalCredentialsPath, credentialsFile.length());
                    }
                } else {
                    // credentialsPath가 없으면 환경 변수 GOOGLE_APPLICATION_CREDENTIALS 확인
                    String envCredentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
                    if (envCredentials != null && !envCredentials.isBlank()) {
                        // 환경 변수가 JSON 내용인지 파일 경로인지 확인
                        if (envCredentials.trim().startsWith("{")) {
                            // JSON 내용으로 직접 제공된 경우
                            jsonContentDirect = envCredentials;
                            logger.info("환경 변수 GOOGLE_APPLICATION_CREDENTIALS가 JSON 내용으로 제공됨");
                        } else {
                            // 파일 경로로 제공된 경우
                            File envFile = new File(envCredentials);
                            if (envFile.exists()) {
                                finalCredentialsPath = envFile.getAbsolutePath();
                                logger.info("환경 변수 GOOGLE_APPLICATION_CREDENTIALS 파일 경로 사용: {}", finalCredentialsPath);
                            } else {
                                logger.warn("환경 변수 GOOGLE_APPLICATION_CREDENTIALS가 파일 경로로 보이지만 파일이 존재하지 않습니다: {}", envCredentials);
                            }
                        }
                    } else {
                        // 기본 경로 확인
                        File defaultCredentials = new File("./google-credentials.json");
                        if (defaultCredentials.exists()) {
                            finalCredentialsPath = defaultCredentials.getAbsolutePath();
                            logger.info("기본 경로에서 credentials 파일 발견: {}", finalCredentialsPath);
                        } else {
                            logger.warn("Google Cloud credentials 파일을 찾을 수 없습니다. " +
                                      "application.yml의 google.cloud.tts.credentials-path를 설정하거나 " +
                                      "환경 변수 GOOGLE_APPLICATION_CREDENTIALS를 설정해주세요.");
                        }
                    }
                }
                
                // credentials 파일이 있거나 JSON 내용이 직접 제공된 경우 로드
                if (finalCredentialsPath != null || jsonContentDirect != null) {
                    try {
                        String jsonContent;
                        
                        if (jsonContentDirect != null) {
                            // JSON 내용이 직접 제공된 경우
                            jsonContent = jsonContentDirect;
                            logger.info("JSON 내용을 직접 사용하여 임시 파일 생성");
                        } else {
                            // 파일에서 읽기
                            byte[] fileBytes = Files.readAllBytes(Paths.get(finalCredentialsPath));
                            jsonContent = new String(fileBytes, StandardCharsets.UTF_8);
                        }
                        
                        // BOM(Byte Order Mark) 제거 (UTF-8 BOM: EF BB BF)
                        if (jsonContent.startsWith("\uFEFF")) {
                            jsonContent = jsonContent.substring(1);
                            logger.info("UTF-8 BOM 제거됨");
                        }
                        
                        // JSON 내용 정리 (앞뒤 공백 제거)
                        jsonContent = jsonContent.trim();
                        
                        // 첫 문자가 '{'인지 확인 (JSON 객체 시작)
                        if (!jsonContent.startsWith("{")) {
                            logger.error("JSON 파일이 '{'로 시작하지 않습니다. 첫 문자: '{}'", 
                                jsonContent.length() > 0 ? jsonContent.charAt(0) : "빈 파일");
                        }
                        
                        // 빈 파일 체크
                        if (jsonContent.isEmpty()) {
                            throw new TtsException(
                                "Google Cloud TTS 인증 파일이 비어있습니다. 파일 경로를 확인해주세요: " + finalCredentialsPath
                            );
                        }
                        
                        // JSON 파싱하여 검증 및 정리
                        try {
                            // JSON 파싱 시도 (Jackson 사용)
                            JsonNode jsonNode = objectMapper.readTree(jsonContent);
                            
                            // 정리된 JSON으로 다시 변환 (불필요한 공백 제거)
                            String cleanedJson = objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(jsonNode);
                            // pretty printer를 사용하지 않고 compact 형식으로
                            cleanedJson = objectMapper.writeValueAsString(jsonNode);
                            
                            // JSON 내용이 직접 제공되었거나 정리가 필요한 경우 임시 파일로 저장
                            if (jsonContentDirect != null || !cleanedJson.equals(jsonContent.trim())) {
                                if (jsonContentDirect != null) {
                                    logger.info("JSON 내용을 임시 파일로 저장");
                                } else {
                                    logger.info("JSON 파일 정리 중 (원본과 차이 발견)");
                                }
                                File tempFile = File.createTempFile("google-credentials-", ".json");
                                Files.write(tempFile.toPath(), cleanedJson.getBytes(StandardCharsets.UTF_8));
                                finalCredentialsPath = tempFile.getAbsolutePath();
                                logger.info("임시 파일 생성 완료: {}", finalCredentialsPath);
                            } else {
                                logger.debug("JSON 파일 형식 검증 완료 (정리 불필요)");
                            }
                            
                            // 환경 변수로 설정 (Google Cloud 라이브러리가 자동으로 찾도록)
                            if (finalCredentialsPath != null) {
                                System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", finalCredentialsPath);
                            }
                        } catch (Exception jsonError) {
                            logger.error("JSON 파일 파싱 실패: {}", jsonError.getMessage());
                            
                            // 더 자세한 에러 정보 로깅
                            String errorMessage = jsonError.getMessage();
                            if (errorMessage != null && errorMessage.contains("line:")) {
                                logger.error("JSON 파싱 오류 위치: {}", errorMessage);
                                
                                // 라인 번호 추출
                                try {
                                    String lineInfo = errorMessage.substring(errorMessage.indexOf("line:"));
                                    String lineNumStr = lineInfo.split(":")[1].split(",")[0].trim();
                                    int lineNum = Integer.parseInt(lineNumStr);
                                    
                                    // 해당 라인 내용 로깅
                                    String[] lines = jsonContent.split("\n");
                                    if (lineNum > 0 && lineNum <= lines.length) {
                                        logger.error("문제가 있는 라인 ({}번째 줄): [{}]", lineNum, lines[lineNum - 1]);
                                        // 해당 라인의 문자를 하나씩 표시
                                        String problemLine = lines[lineNum - 1];
                                        StringBuilder charInfo = new StringBuilder();
                                        for (int i = 0; i < Math.min(problemLine.length(), 100); i++) {
                                            char c = problemLine.charAt(i);
                                            charInfo.append(String.format("'%c'(%d) ", c, (int)c));
                                        }
                                        logger.error("라인 문자 정보: {}", charInfo.toString());
                                    }
                                } catch (Exception e) {
                                    logger.debug("라인 정보 파싱 실패", e);
                                }
                            }
                            
                            // JSON 내용의 처음과 끝 부분 로깅
                            int previewLength = Math.min(500, jsonContent.length());
                            logger.error("JSON 내용 (처음 {}자): {}", previewLength, 
                                jsonContent.substring(0, previewLength));
                            
                            // 2번째 줄 주변 내용 상세 로깅 (오류 발생 위치)
                            String[] lines = jsonContent.split("\n");
                            if (lines.length >= 2) {
                                logger.error("2번째 줄 내용: [{}]", lines[1]);
                                // 2번째 줄의 각 문자를 상세히 표시
                                StringBuilder charDetails = new StringBuilder();
                                String line2 = lines[1];
                                for (int i = 0; i < Math.min(line2.length(), 50); i++) {
                                    char c = line2.charAt(i);
                                    charDetails.append(String.format("'%c'(%d) ", c, (int)c));
                                }
                                logger.error("2번째 줄 문자 상세 (처음 50자): {}", charDetails.toString());
                            }
                            
                            if (jsonContent.length() > 500) {
                                logger.error("JSON 내용 (마지막 200자): {}", 
                                    jsonContent.substring(jsonContent.length() - 200));
                            }
                            
                            // 전체 파일 내용을 라인별로 로깅 (처음 10줄)
                            logger.error("파일 내용 (처음 10줄):");
                            for (int i = 0; i < Math.min(10, lines.length); i++) {
                                logger.error("  라인 {}: [{}]", i + 1, lines[i]);
                            }
                            
                            // 파일 크기와 첫 몇 바이트를 16진수로 로깅 (BOM이나 특수 문자 확인)
                            if (jsonContentDirect == null && finalCredentialsPath != null) {
                                try {
                                    byte[] fileBytes = Files.readAllBytes(Paths.get(finalCredentialsPath));
                                    logger.error("파일 크기: {} bytes", fileBytes.length);
                                    if (fileBytes.length > 0) {
                                        StringBuilder hexPreview = new StringBuilder();
                                        int hexLength = Math.min(100, fileBytes.length);
                                        for (int i = 0; i < hexLength; i++) {
                                            hexPreview.append(String.format("%02X ", fileBytes[i]));
                                            if ((i + 1) % 16 == 0) {
                                                hexPreview.append("\n");
                                            }
                                        }
                                        logger.error("파일 첫 {} 바이트 (16진수):\n{}", hexLength, hexPreview.toString());
                                    }
                                } catch (Exception e) {
                                    logger.debug("파일 바이트 정보 읽기 실패", e);
                                }
                            }
                            
                            throw new TtsException(
                                "Google Cloud TTS 인증 파일 JSON 형식 오류: " + jsonError.getMessage() + 
                                ". 파일이 올바른 JSON 형식인지 확인해주세요. 파일 경로: " + finalCredentialsPath, 
                                jsonError
                            );
                        }
                        
                        // ServiceAccountCredentials로 직접 로드
                        try (FileInputStream credentialsStream = new FileInputStream(finalCredentialsPath)) {
                            GoogleCredentials credentials = ServiceAccountCredentials
                                .fromStream(credentialsStream)
                                .createScoped(
                                    java.util.Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")
                                );
                            settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
                            logger.info("Google Cloud credentials 로드 완료");
                        }
                    } catch (IOException e) {
                        logger.error("Google Cloud credentials 파일 읽기 실패: {}", finalCredentialsPath, e);
                        throw new TtsException(
                            "Google Cloud TTS 인증 파일 읽기 실패: " + e.getMessage(), e
                        );
                    } catch (Exception e) {
                        logger.error("Google Cloud credentials 처리 중 오류: {}", finalCredentialsPath, e);
                        throw new TtsException(
                            "Google Cloud TTS 인증 파일 처리 실패: " + e.getMessage(), e
                        );
                    }
                } else {
                    // credentials 파일이 없으면 기본 인증 사용 (환경 변수에서 자동으로 찾음)
                    logger.info("Google Cloud 기본 인증 사용 (환경 변수 또는 기본 위치에서 자동 검색)");
                }
                
                logger.info("Google Cloud TTS 클라이언트 초기화 중...");
                textToSpeechClient = TextToSpeechClient.create(settingsBuilder.build());
                logger.info("Google Cloud TTS 클라이언트 초기화 완료");
            } catch (IOException e) {
                String errorMsg = e.getMessage();
                logger.error("Google Cloud TTS 클라이언트 초기화 실패: {}", errorMsg, e);
                
                if (errorMsg != null && errorMsg.contains("credentials were not found")) {
                    throw new TtsException(
                        "Google Cloud TTS 인증 정보를 찾을 수 없습니다. " +
                        "application.yml에 google.cloud.tts.credentials-path를 설정하거나 " +
                        "환경 변수 GOOGLE_APPLICATION_CREDENTIALS를 설정해주세요. " +
                        "자세한 내용: https://cloud.google.com/docs/authentication/external/set-up-adc",
                        e
                    );
                }
                throw new TtsException("Google Cloud TTS 클라이언트 초기화 실패: " + errorMsg, e);
            } catch (Exception e) {
                logger.error("Google Cloud TTS 클라이언트 초기화 중 예상치 못한 오류 발생", e);
                throw new TtsException("Google Cloud TTS 클라이언트 초기화 실패: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public byte[] synthesize(String text, String voiceType) {
        if (text == null || text.isBlank()) {
            throw new TtsException("변환할 텍스트가 비어있습니다.");
        }

        try {
            initializeClient();

            // 음성 설정
            String voiceName = mapVoiceType(voiceType);
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("ko-KR")  // 한국어
                    .setName(voiceName)  // 목소리 선택 (voice name에 성별이 포함됨)
                    .build();

            // 오디오 설정 (더 자연스러운 음성을 위해 파라미터 조절)
            // speakingRate를 약간 낮추면(0.9~0.95) 더 자연스럽고 감정이 있는 목소리가 됩니다
            // voiceType에 따라 pitch를 조절 (여성: 높게, 남성: 낮게)
            boolean isMaleVoice = voiceType != null && 
                    (voiceType.toUpperCase().equals("TYPE4") || 
                     voiceType.toUpperCase().equals("TYPE5") || 
                     voiceType.toUpperCase().equals("TYPE6"));
            double pitch = isMaleVoice ? -2.0 : 2.0;  // 남성: 낮게, 여성: 높게
            
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)  // MP3 형식
                    .setSpeakingRate(0.95)  // 말하기 속도 (0.9~0.95가 더 자연스러움)
                    .setPitch(pitch)  // 음높이 조절 (여성: +2.0, 남성: -2.0)
                    .setVolumeGainDb(0.0)  // 볼륨 조절 (-96.0 ~ 16.0, 0.0이 정상)
                    .build();

            // SSML을 사용하여 더 자연스러운 음성 생성
            // 감정 표현을 위해 텍스트를 SSML로 감싸기
            String ssmlText = wrapWithSsml(text);
            
            // TTS 요청 생성
            logger.debug("TTS 요청 생성 중 - 텍스트 길이: {}, VoiceType: {}, VoiceName: {}", 
                        text.length(), voiceType, voiceName);
            SynthesizeSpeechRequest request = SynthesizeSpeechRequest.newBuilder()
                    .setInput(SynthesisInput.newBuilder().setSsml(ssmlText).build())
                    .setVoice(voice)
                    .setAudioConfig(audioConfig)
                    .build();

            // TTS API 호출
            logger.info("Google Cloud TTS API 호출 중...");
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(request);
            logger.info("Google Cloud TTS API 호출 성공");

            // 오디오 데이터 추출
            ByteString audioContents = response.getAudioContent();
            if (audioContents == null || audioContents.isEmpty()) {
                logger.error("TTS API 응답이 비어있습니다.");
                throw new TtsException("TTS API 응답이 비어있습니다.");
            }
            
            logger.debug("오디오 데이터 추출 완료 - 크기: {} bytes", audioContents.size());
            return audioContents.toByteArray();

        } catch (Exception e) {
            if (e instanceof TtsException) {
                throw e;
            }
            logger.error("Google Cloud TTS API 호출 실패: {}", e.getMessage(), e);
            throw new TtsException("Google Cloud TTS API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 캐릭터의 VoiceType을 Google Cloud TTS voice로 매핑합니다.
     * 무료 모델만 사용합니다:
     * 
     * 여성 목소리:
     * - ko-KR-Standard-A (여성, 표준, 무료)
     * - ko-KR-Standard-C (여성, 표준, 무료)
     * - ko-KR-Wavenet-A (여성, 고품질, 무료 티어 제공)
     * - ko-KR-Wavenet-C (여성, 고품질, 무료 티어 제공)
     * 
     * 남성 목소리:
     * - ko-KR-Standard-B (남성, 표준, 무료)
     * - ko-KR-Standard-D (남성, 표준, 무료)
     * - ko-KR-Wavenet-B (남성, 고품질, 무료 티어 제공)
     * - ko-KR-Wavenet-D (남성, 고품질, 무료 티어 제공)
     * 
     * AudioConfig의 파라미터(speakingRate, pitch 등)를 조절하여 더 자연스럽게 만들 수 있습니다.
     */
    private String mapVoiceType(String voiceType) {
        // 기본값은 "ko-KR-Wavenet-A" (고품질 여성 목소리, 무료 티어 제공)
        return switch (voiceType != null ? voiceType.toUpperCase() : "") {
            // 여성 목소리
            case "TYPE1" -> "ko-KR-Wavenet-A";  // 부드러운 여성 목소리 (고품질, 무료 티어)
            case "TYPE2" -> "ko-KR-Wavenet-C";  // 밝은 여성 목소리 (고품질, 무료 티어)
            case "TYPE3" -> "ko-KR-Standard-A"; // 표준 여성 목소리 (무료)
            // 남성 목소리
            case "TYPE4" -> "ko-KR-Wavenet-B";  // 부드러운 남성 목소리 (고품질, 무료 티어)
            case "TYPE5" -> "ko-KR-Wavenet-D";  // 밝은 남성 목소리 (고품질, 무료 티어)
            case "TYPE6" -> "ko-KR-Standard-B"; // 표준 남성 목소리 (무료)
            default -> "ko-KR-Wavenet-A";
        };
    }

    /**
     * 텍스트를 SSML로 감싸서 더 자연스러운 음성을 생성합니다.
     * SSML을 사용하면 감정 표현, 강조, 휴지 등을 추가할 수 있습니다.
     */
    private String wrapWithSsml(String text) {
        // 기본 SSML 래퍼
        // AudioConfig에서 이미 speakingRate와 pitch를 조절하고 있으므로
        // 여기서는 기본 래퍼만 사용
        // 필요시 나중에 더 복잡한 SSML 처리 추가 가능
        return String.format(
            "<speak>%s</speak>",
            text
        );
    }
}

