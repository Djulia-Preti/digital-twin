package br.senai.twin.model;

public class Command {

    private Led led;
    private Lcd lcd;

    public Led getLed() { return led; }
    public void setLed(Led led) { this.led = led; }

    public Lcd getLcd() { return lcd; }
    public void setLcd(Lcd lcd) { this.lcd = lcd; }
}
