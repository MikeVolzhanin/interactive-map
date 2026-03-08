package ru.volzhanin.applicantsservice.dto;

import lombok.Data;

@Data
public class SendCodeRequestDto {
    private String phoneNumber;
}
