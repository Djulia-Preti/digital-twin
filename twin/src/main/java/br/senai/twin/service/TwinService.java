package br.senai.twin.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.senai.twin.model.Buttons;
import br.senai.twin.model.Command;
import br.senai.twin.model.EspMessage;
import br.senai.twin.model.Snapshot;
import br.senai.twin.model.Status;
import br.senai.twin.model.Telemetry;
import jakarta.annotation.PostConstruct;

@Service
public class TwinService implements Esp32Listener {

    @Autowired
    private Esp32Service esp32;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private volatile Snapshot last = new Snapshot();

    @PostConstruct
    public void init() {
        esp32.setListener(this);
    }

    public Snapshot getLast() {
        return last;
    }

    // o ESP32 empurra os dados: telemetria a cada 1s e botão na hora que muda
    @Override
    public synchronized void onMessage(EspMessage message) {
        if ("telemetry".equals(message.getType()) && message.getData() != null) {
            publish(Status.ONLINE, message.getData());
        } else if ("button".equals(message.getType()) && message.getId() != null && message.getPressed() != null) {
            Telemetry telemetry = last.getTelemetry() != null ? last.getTelemetry() : new Telemetry();
            if (telemetry.getButtons() == null) {
                telemetry.setButtons(new Buttons());
            }
            telemetry.getButtons().press(message.getId(), message.getPressed());
            publish(Status.ONLINE, telemetry);
        }
    }

    // ESP32 fora: mantém a última leitura e marca offline
    @Override
    public synchronized void onDisconnect() {
        Snapshot snapshot = new Snapshot();
        snapshot.setStatus(Status.OFFLINE);
        snapshot.setTelemetry(last.getTelemetry());
        snapshot.setUpdatedAt(last.getUpdatedAt());
        last = snapshot;
        broadcast(snapshot);
    }

    public void sendCommand(Command command) {
        esp32.sendCommand(command);
    }

    public synchronized SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        send(emitter, last);
        return emitter;
    }

    private void publish(Status status, Telemetry telemetry) {
        Snapshot snapshot = new Snapshot();
        snapshot.setStatus(status);
        snapshot.setTelemetry(telemetry);
        snapshot.setUpdatedAt(Instant.now());
        last = snapshot;
        broadcast(snapshot);
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
