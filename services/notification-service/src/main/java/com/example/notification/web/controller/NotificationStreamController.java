package com.example.notification.web.controller;

import com.example.notification.service.NotificationStreamService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
public class NotificationStreamController {

    private final NotificationStreamService streamService;

    public NotificationStreamController(NotificationStreamService streamService) {
        this.streamService = streamService;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return streamService.subscribe();
    }
}
