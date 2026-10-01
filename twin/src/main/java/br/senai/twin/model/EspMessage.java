package br.senai.twin.model;

// mensagem que chega do ESP32 pelo WebSocket
// {"type":"telemetry","data":{...}}  ou  {"type":"button","id":1,"pressed":true}
public class EspMessage {

    private String type;
    private Telemetry data;
    private Integer id;
    private Boolean pressed;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Telemetry getData() { return data; }
    public void setData(Telemetry data) { this.data = data; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Boolean getPressed() { return pressed; }
    public void setPressed(Boolean pressed) { this.pressed = pressed; }
}
