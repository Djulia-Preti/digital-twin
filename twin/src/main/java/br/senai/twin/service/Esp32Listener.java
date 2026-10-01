package br.senai.twin.service;

import br.senai.twin.model.EspMessage;

// quem quiser ser avisado do que acontece na conexão com o ESP32
public interface Esp32Listener {

    void onMessage(EspMessage message);

    void onDisconnect();
}
