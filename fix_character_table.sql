-- Character 테이블 수정 SQL
-- H2 콘솔에서 실행하세요

-- 기존 테이블이 있다면 삭제 (주의: 기존 데이터가 모두 삭제됩니다)
DROP TABLE IF EXISTS "Character";

-- Character 테이블 생성
CREATE TABLE "Character" (
    "캐릭터ID" BIGINT AUTO_INCREMENT PRIMARY KEY,
    "관계" VARCHAR(50) NOT NULL,
    "만난 날짜" TIMESTAMP,
    "헤어지는 날짜" TIMESTAMP,
    "애정도" INTEGER NOT NULL DEFAULT 0,
    "캐릭터 소개" TEXT,
    "이미지" VARCHAR(255),
    "목소리" VARCHAR(50) NOT NULL,
    "MBTI" VARCHAR(50) NOT NULL,
    "성별" VARCHAR(50) NOT NULL,
    "이름" VARCHAR(50) NOT NULL,
    "애착타입" VARCHAR(50) NOT NULL,
    "테토력" INTEGER NOT NULL
);

