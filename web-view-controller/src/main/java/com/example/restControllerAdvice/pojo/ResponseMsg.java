package com.example.restControllerAdvice.pojo;

public class ResponseMsg {
    private String massage;

    public ResponseMsg(String massage) {
        this.massage = massage;
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }
}
