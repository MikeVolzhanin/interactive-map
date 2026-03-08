package ru.volzhanin.applicantsservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.volzhanin.applicantsservice.dto.SendCodeRequestDto;
import ru.volzhanin.applicantsservice.dto.VerifyCodeRequestDto;
import ru.volzhanin.applicantsservice.service.PhoneAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final PhoneAuthService phoneAuthService;
    private final UserDetailsService userDetailsService;

    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestBody SendCodeRequestDto request) {
        phoneAuthService.sendCode(request.getPhoneNumber());
        return ResponseEntity.ok("Code sent");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody VerifyCodeRequestDto request,
                                             HttpServletRequest httpServletRequest,
                                             HttpServletResponse httpServletResponse) {

        phoneAuthService.verifyCode(request.getPhoneNumber(), request.getCode());

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getPhoneNumber());

        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        securityContextRepository.saveContext(context, httpServletRequest, httpServletResponse);

        return ResponseEntity.ok("Authenticated");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out");
    }
}
