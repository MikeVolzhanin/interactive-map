package ru.volzhanin.applicantsservice.dto;

import lombok.Data;

@Data
public class VerifyCodeRequestDto {
    private String phoneNumber;
    private String code;
}
