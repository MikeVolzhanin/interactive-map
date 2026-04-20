package ru.volzhanin.applicantsservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.volzhanin.applicantsservice.dto.InterestDto;
import ru.volzhanin.applicantsservice.service.InterestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {
    private final InterestService interestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<InterestDto> getAll() {
        return interestService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public InterestDto getById(@PathVariable Long id) {
        return interestService.getByInterestId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public InterestDto create(@RequestBody InterestDto dto) {
        return interestService.create(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public InterestDto update(@PathVariable Long id, @RequestBody InterestDto dto) {
        return interestService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        interestService.delete(id);
    }
}
