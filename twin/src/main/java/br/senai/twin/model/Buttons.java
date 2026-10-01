package br.senai.twin.model;

public class Buttons {
    private Boolean b1 = false;
    private Boolean b2 = false;
    private Boolean b3 = false;
    private Boolean b4 = false;

    public Boolean getB1() { return b1; }
    public void setB1(Boolean b1) { this.b1 = b1; }

    public Boolean getB2() { return b2; }
    public void setB2(Boolean b2) { this.b2 = b2; }

    public Boolean getB3() { return b3; }
    public void setB3(Boolean b3) { this.b3 = b3; }

    public Boolean getB4() { return b4; }
    public void setB4(Boolean b4) { this.b4 = b4; }

    // id do botão (1 a 4) igual ao enviado pelo ESP32
    public void press(int id, boolean pressed) {
        switch (id) {
            case 1 -> b1 = pressed;
            case 2 -> b2 = pressed;
            case 3 -> b3 = pressed;
            case 4 -> b4 = pressed;
            default -> { }
        }
    }
}
