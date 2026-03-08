package ru.volzhanin.applicantsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.repository.UsersRepository;
import ru.volzhanin.applicantsservice.sms.SmsSender;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PhoneAuthService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final SmsSender smsSender;
    private final UsersRepository userRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public void sendCode(String phoneNumber) {
        userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found: " + phoneNumber));

        String code = generateCode();
        String key = buildOtpKey(phoneNumber);

        stringRedisTemplate.opsForValue().set(key, code, OTP_TTL);

        smsSender.sendCode(phoneNumber, "Ваш код подтверждения: " + code);
    }

    public void verifyCode(String phoneNumber, String code) {
        String key = buildOtpKey(phoneNumber);
        String savedCode = stringRedisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new RuntimeException("Code expired or not found");
        }

        if (!savedCode.equals(code)) {
            throw new RuntimeException("Invalid code");
        }

        stringRedisTemplate.delete(key);
    }

    private String generateCode() {
        int value = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(value);
    }

    private String buildOtpKey(String phoneNumber) {
        return "auth:otp:" + phoneNumber;
    }
}
