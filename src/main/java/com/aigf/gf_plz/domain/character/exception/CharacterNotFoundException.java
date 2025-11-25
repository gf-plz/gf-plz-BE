package com.aigf.gf_plz.domain.character.exception;

/**
 * 캐릭터를 찾을 수 없을 때 발생하는 예외
 */
public class CharacterNotFoundException extends RuntimeException {
    
    public CharacterNotFoundException(Long characterId) {
        super("캐릭터를 찾을 수 없습니다. ID: " + characterId);
    }
    
    public CharacterNotFoundException(String message) {
        super(message);
    }
}




