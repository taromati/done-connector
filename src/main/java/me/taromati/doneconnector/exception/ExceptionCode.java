package me.taromati.doneconnector.exception;

import lombok.Getter;

@Getter
public enum ExceptionCode {
    CONFIG_LOAD_ERROR("CONFIG_LOAD_ERROR", "설정 파일을 불러오는 중 오류가 발생했습니다."),/**/
    ID_NOT_FOUND("ID_NOT_FOUND", "치지직 아이디가 없습니다."),/**/
    REWARD_NOT_FOUND("REWARD_NOT_FOUND", "후원 목록이 없습니다."),/**/
    REWARD_PARSE_ERROR("REWARD_PARSE_ERROR", "후원 목록을 파싱하는 중 오류가 발생했습니다."),/**/
    API_CHAT_CHANNEL_ID_ERROR("API_CHAT_CHANNEL_ID_ERROR", "채널 아이디 조희를 실패했습니다."),/**/
    API_ACCESS_TOKEN_ERROR("API_ACCESS_TOKEN_ERROR", "액세스 토큰을 발급받는 중 오류가 발생했습니다."),/**/
    ;

    private final String code;

    private final String message;

    ExceptionCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
