package com.example.ledger.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Manages the list of active Server-Sent Events subscribers and broadcasts a
 * lightweight signal every time a transaction changes so mobile clients can
 * refresh their local cache.
 */
@Component
public class TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);
    private static final long EMITTER_TIMEOUT = Duration.ofMinutes(15).toMillis();

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("listening"));
        } catch (IOException e) {
            emitters.remove(emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void publishTransactionSignal(String eventName) {
        List<SseEmitter> staleEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data("refresh"));
            } catch (IOException exception) {
                log.debug("Removing stale SSE emitter: {}", exception.getMessage());
                staleEmitters.add(emitter);
            }
        }
        emitters.removeAll(staleEmitters);
    }
}
