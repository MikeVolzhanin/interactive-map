package ru.volzhanin.applicantsservice.sms;

public interface SmsSender {
    void sendCode(String phoneNumber, String message);
}
