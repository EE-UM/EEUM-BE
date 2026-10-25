package com.eeum.domain.admin.exception;

/**
 * 관리자 조치를 수행할 수 없는 상태. 메시지는 관리자 화면에 그대로 노출된다.
 */
public class InvalidAdminActionException extends RuntimeException {

    public InvalidAdminActionException(String message) {
        super(message);
    }
}
