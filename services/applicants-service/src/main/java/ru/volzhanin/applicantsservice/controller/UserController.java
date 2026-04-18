package ru.volzhanin.applicantsservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.volzhanin.applicantsservice.dto.export.ExportRequest;
import ru.volzhanin.applicantsservice.dto.user.UserInfoDto;
import ru.volzhanin.applicantsservice.dto.user.UserInterestsDto;
import ru.volzhanin.applicantsservice.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/add-info")
    public ResponseEntity<?> addUserInfo(@RequestBody UserInfoDto userInfoDto) {
        return userService.addInfo(userInfoDto);
    }

    @GetMapping("/get-info")
    public ResponseEntity<?> getUserInfo() {
        return userService.getUserInfo();
    }

    @PostMapping("/change-interests")
    public ResponseEntity<?> changeInterests(@RequestBody UserInterestsDto userInterestsDto) {
        return userService.changeInterests(userInterestsDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportUsers(@RequestBody ExportRequest request) {
        StreamingResponseBody stream = outputStream ->
                userService.writeUsersToStream(request.getFields(), outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(stream);
    }
}
