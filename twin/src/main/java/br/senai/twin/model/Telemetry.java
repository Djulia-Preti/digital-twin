package br.senai.twin.model;

public class Telemetry {
    private Double temperature;
    private Double humidity;
    private Double distance;
    private Integer adc;
    
    // Usando Led e Lcd para ficar igual ao Command.java
    private Led led;
    private Lcd lcd;
    private Buttons buttons;

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
    
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    
    public Integer getAdc() { return adc; }
    public void setAdc(Integer adc) { this.adc = adc; }
    
    public Led getLed() { return led; }
    public void setLed(Led led) { this.led = led; }
    
    public Lcd getLcd() { return lcd; }
    public void setLcd(Lcd lcd) { this.lcd = lcd; }

    public Buttons getButtons() { return buttons; }
    public void setButtons(Buttons buttons) { this.buttons = buttons; }
}