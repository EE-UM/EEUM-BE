package com.eeum.domain.notification.fcm.port;

import com.eeum.domain.notification.fcm.port.dto.response.PushResult;
import java.util.List;
import java.util.Map;

public interface PushSender {

    PushResult send(List<String> tokens, String title, String body, Map<String, String> data);
}
