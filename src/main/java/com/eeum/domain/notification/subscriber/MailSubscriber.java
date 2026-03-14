//package com.eeum.domain.notification.subscriber;
//
//import com.eeum.domain.notification.dto.NotificationMessageDto;
//import com.eeum.domain.notification.service.NotificationService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessageListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class MailSubscriber implements MessageListener {
//
//    private final ObjectMapper objectMapper;
//    private final NotificationService notificationService;
//
//
//
//    @Override
//    public void onMessage(Message message) {
//        try {
//            String jsonBody = new String(message.getBody());
//
//            NotificationMessageDto notification = objectMapper.readValue(jsonBody, NotificationMessageDto.class);
//
//            notificationService.handleNotification(notification);
//        } catch (JsonProcessingException e) {
//            log.error("알림 메시지 역직렬화 실패", e);
//        }
//    }
//}
