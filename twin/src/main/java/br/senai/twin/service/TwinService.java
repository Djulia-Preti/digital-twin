package br.senai.twin.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.senai.twin.model.Command;
import br.senai.twin.model.Snapshot;
import br.senai.twin.model.Status;
import br.senai.twin.model.Telemetry;

@Service
public class TwinService {

    @Autowired
    private Esp32Service esp32;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private volatile Snapshot last = new Snapshot();

    public Snapshot getLast() {
        return last;
    }

    // a cada 1s pergunta pro ESP32 e manda pro front
    @Scheduled(fixedDelay = 3000)
    public void poll() {
        Snapshot snapshot = new Snapshot();
        try {
            Telemetry telemetry = esp32.getTelemetry();
            snapshot.setStatus(Status.ONLINE);
            snapshot.setTelemetry(telemetry);
            snapshot.setUpdatedAt(Instant.now());
        } catch (RestClientException e) {
            // ESP32 fora: mantém a última leitura e marca offline
            snapshot.setStatus(Status.OFFLINE);
            snapshot.setTelemetry(last.getTelemetry());
            snapshot.setUpdatedAt(last.getUpdatedAt());
        }
        last = snapshot;
        broadcast(snapshot);
    }

    public void sendCommand(Command command) {
        esp32.sendCommand(command);
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        send(emitter, last);
        return emitter;
    }

    private void broadcast(Snapshot snapshot) {
        for (SseEmitter emitter : emitters) {
            send(emitter, snapshot);
        }
    }

    private void send(SseEmitter emitter, Snapshot snapshot) {
        try {
            emitter.send(SseEmitter.event().name("snapshot").data(snapshot, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            emitters.remove(emitter);
        }
    }
}
