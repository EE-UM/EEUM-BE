package com.eeum.domain.admin.support;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/**
 * 관리자 화면 시간 표시용. created_at 등은 JVM 기본 시간대(운영 컨테이너 UTC)로 저장되므로 KST 로 바꿔 보여준다.
 * 템플릿에서 ${@adminTime.format(value)} 로 사용한다.
 */
@Component("adminTime")
public class AdminTimeFormatter {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ZoneId storedZone;

    public AdminTimeFormatter() {
        this(ZoneId.systemDefault());
    }

    AdminTimeFormatter(ZoneId storedZone) {
        this.storedZone = storedZone;
    }

    public String format(LocalDateTime storedTime) {
        if (storedTime == null) {
            return "-";
        }
        return storedTime.atZone(storedZone).withZoneSameInstant(KST).format(FORMAT);
    }
}
