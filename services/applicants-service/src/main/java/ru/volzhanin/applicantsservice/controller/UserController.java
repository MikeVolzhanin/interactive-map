package ru.volzhanin.applicantsservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.volzhanin.applicantsservice.dto.user.UserInfoDto;
import ru.volzhanin.applicantsservice.dto.user.UserInterestsDto;
import ru.volzhanin.applicantsservice.service.UserService;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/add-info")
    public ResponseEntity<?> addUserInfo(@RequestBody UserInfoDto userInfoDto) {
        return userService.addInfo(userInfoDto);
    }

    @PostMapping("/change-interests")
    public ResponseEntity<?> changeInterests(@RequestBody UserInterestsDto userInterestsDto) {
        return userService.changeInterests(userInterestsDto);
    }
}
