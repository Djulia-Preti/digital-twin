package br.senai.twin.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import br.senai.twin.model.Command;
import br.senai.twin.model.EspMessage;
import tools.jackson.databind.json.JsonMapper;

@Service
public class Esp32Service {

    private static final Logger log = LoggerFactory.getLogger(Esp32Service.class);

    // se o ESP32 ficar esse tempo sem mandar nada, considera que caiu
    private static final long TIMEOUT_SEGUNDOS = 5;

    @Value("${esp32.url}")
    private String url;

    private final JsonMapper mapper;
    private final HttpClient http = HttpClient.newHttpClient();
    private final Object envio = new Object();

    private volatile WebSocket socket;
    private volatile boolean conectando = false;
    private volatile Instant ultimaMensagem = Instant.EPOCH;
    private volatile Esp32Listener listener;

    public Esp32Service(JsonMapper mapper) {
        this.mapper = mapper;
    }

    public void setListener(Esp32Listener listener) {
        this.listener = listener;
    }

    // a cada 2s: derruba conexão que ficou muda e reconecta se estiver sem socket
    @Scheduled(fixedDelay = 2000)
    public void manterConexao() {
        WebSocket atual = socket;
        if (atual != null && Duration.between(ultimaMensagem, Instant.now()).getSeconds() > TIMEOUT_SEGUNDOS) {
            log.warn("ESP32 sem resposta, fechando conexão");
            atual.abort();
            desconectou(atual);
        }
        if (socket == null && !conectando) {
            conectar();
        }
    }

    private void conectar() {
        conectando = true;
        http.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .buildAsync(URI.create(url), new Receptor())
                .whenComplete((ws, erro) -> conectando = false);
    }

    public void sendCommand(Command command) {
        WebSocket ws = socket;
        if (ws == null) {
            throw new IllegalStateException("ESP32 desconectado");
        }
        String json = mapper.writeValueAsString(command);
        // o WebSocket do Java não aceita dois envios ao mesmo tempo
        synchronized (envio) {
            try {
                ws.sendText(json, true).orTimeout(3, TimeUnit.SECONDS).join();
            } catch (CompletionException e) {
                throw new IllegalStateException("Falha ao enviar comando ao ESP32", e);
            }
        }
    }

    private void desconectou(WebSocket ws) {
        if (socket != ws) {
            return; // já tratado
        }
        socket = null;
        if (listener != null) {
            listener.onDisconnect();
        }
    }

    private class Receptor implements WebSocket.Listener {

        // mensagem grande pode chegar em pedaços
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket ws) {
            log.info("Conectado ao ESP32 em {}", url);
            socket = ws;
            ultimaMensagem = Instant.now();
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                String texto = buffer.toString();
                buffer.setLength(0);
                ultimaMensagem = Instant.now();
                tratar(texto);
            }
            ws.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            log.info("ESP32 fechou a conexão ({})", statusCode);
            desconectou(ws);
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            log.warn("Erro na conexão com o ESP32: {}", error.getMessage());
            desconectou(ws);
        }

        private void tratar(String texto) {
            try {
                EspMessage message = mapper.readValue(texto, EspMessage.class);
                if (listener != null) {
                    listener.onMessage(message);
                }
            } catch (RuntimeException e) {
                log.warn("Mensagem inválida do ESP32: {}", texto);
            }
        }
    }
}
