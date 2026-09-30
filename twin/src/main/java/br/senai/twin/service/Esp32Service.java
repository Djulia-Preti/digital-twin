package main.java.br.senai.twin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.senai.twin.model.Command;
import br.senai.twin.model.Telemetry;

@Service
public class Esp32Service {

    @Value("${twin.esp32.base-url}")
    private String baseUrl;

    private final RestTemplate rest;

    public Esp32Service() {
        // timeout curto pra não travar o polling quando o ESP32 cai
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1500);
        factory.setReadTimeout(1500);
        this.rest = new RestTemplate(factory);
    }

    public Telemetry getTelemetry() {
        return rest.getForObject(baseUrl + "/telemetry", Telemetry.class);
    }

    public void sendCommand(Command command) {
        rest.postForObject(baseUrl + "/command", command, String.class);
    }
}
