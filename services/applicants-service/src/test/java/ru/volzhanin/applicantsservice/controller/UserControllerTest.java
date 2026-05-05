package ru.volzhanin.applicantsservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.volzhanin.applicantsservice.dto.export.ExportRequest;
import ru.volzhanin.applicantsservice.dto.user.UserInfoDto;
import ru.volzhanin.applicantsservice.dto.user.UserInterestsDto;
import ru.volzhanin.applicantsservice.service.UserService;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final UserController controller = new UserController(userService);

    @Test
    void profileEndpoints_delegateToUserService() {
        UserInfoDto info = UserInfoDto.builder()
            .firstName("Ivan")
            .lastName("Ivanov")
            .phoneNumber("+79991234567")
            .build();
        when(userService.getUserInfo()).thenReturn(info);
        UserInterestsDto interests = new UserInterestsDto(Set.of(1L));

        controller.addUserInfo(info);
        UserInfoDto result = controller.getUserInfo();
        controller.changeInterests(interests);

        assertThat(result).isSameAs(info);
        verify(userService).addInfo(info);
        verify(userService).changeInterests(interests);
    }

    @Test
    void exportUsers_returnsStreamingExcelResponse() throws Exception {
        ExportRequest request = new ExportRequest(List.of("email", "firstName"));

        ResponseEntity<StreamingResponseBody> response = controller.exportUsers(request);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getBody().writeTo(outputStream);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("users.xlsx");
        assertThat(response.getHeaders().getContentType())
            .hasToString("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(userService).writeUsersToStream(request.getFields(), outputStream);
    }
}
