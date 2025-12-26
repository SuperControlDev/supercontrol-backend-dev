package com.supercontrol.backend.domain.session;

public enum EndReason {

    /** 사용자가 종료 버튼 클릭 */
    USER_END,

    /** 게임 시간 초과 */
    TIMEOUT,

    /** 네트워크 끊김 */
    DISCONNECTED,

    /** 서버 강제 종료 */
    SERVER_FORCE
}
