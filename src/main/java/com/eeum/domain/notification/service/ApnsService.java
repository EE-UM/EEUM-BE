package com.eeum.domain.notification.service;

import com.eatthepath.pushy.apns.ApnsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApnsService {

    private final ApnsClient apnsClient;
}
