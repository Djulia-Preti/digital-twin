package br.senai.twin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.senai.twin.model.Command;
import br.senai.twin.model.Telemetry;
import org.springframework.http.client.BufferingClientHttpRequestFactory;

@Service
public class Esp32Service {

    @Value("${esp32.url}")
    private String baseUrl;

    private final RestTemplate rest;

    public Esp32Service() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.rest = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
    }

    public Telemetry getTelemetry() {
        return rest.getForObject(baseUrl + "/telemetry", Telemetry.class);
    }

    public void sendCommand(Command command) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // FORÇA O ESP32 A NÃO MANTER A CONEXÃO ABERTA
        headers.set("Connection", "close"); 

        HttpEntity<Command> entity = new HttpEntity<>(command, headers);

        // Dispara o POST e processa a resposta como String simples
        rest.postForObject(baseUrl + "/command", entity, String.class);
    }
}