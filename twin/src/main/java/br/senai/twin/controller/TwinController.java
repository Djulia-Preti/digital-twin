package br.senai.twin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.senai.twin.model.Command;
import br.senai.twin.model.Snapshot;
import br.senai.twin.service.TwinService;

@RestController
@RequestMapping("/api/twin")
@CrossOrigin(origins = "*") // Permite requisições do seu Frontend
public class TwinController {

    @Autowired
    private TwinService service;

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events() {
        return service.subscribe();
    }

    @GetMapping("/telemetry")
    public Snapshot telemetry() {
        return service.getLast();
    }

    @PostMapping("/command")
    public ResponseEntity<Void> command(@RequestBody Command command) {
        try {
            service.sendCommand(command);
            return ResponseEntity.accepted().build();
        } catch (RestClientException e) {
            System.err.println("Erro ao comunicar com ESP32: " + e.getMessage());
            e.printStackTrace(); // Exibe a pilha de erro real no terminal do Spring Boot
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
