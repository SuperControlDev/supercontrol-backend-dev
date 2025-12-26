package com.supercontrol.backend.domain.session;

public enum PlaySessionStatus {

    /** 게임 진행 중 */
    PLAYING,

    /** 정상 종료 */
    ENDED,

    /** 서버 또는 비정상 종료 */
    FORCED_ENDED
}
