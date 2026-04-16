package ru.volzhanin.applicantsservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.volzhanin.applicantsservice.dto.EducationLevelDto;
import ru.volzhanin.applicantsservice.service.EducationLevelService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/education-levels")
public class EducationLevelController {
    private final EducationLevelService educationLevelService;

    @GetMapping
    public List<EducationLevelDto> getAllEducationLevels() {
        return educationLevelService.getAllEducationLevel();
    }

    @GetMapping("/{id}")
    public EducationLevelDto getEducationLevel(@PathVariable Integer id) {
        return educationLevelService.getEducationLevelById(id);
    }

    @PostMapping
    public EducationLevelDto create(@RequestBody @Valid EducationLevelDto dto) {
        return educationLevelService.create(dto);
    }

    @PatchMapping("/{id}")
    public EducationLevelDto update(@PathVariable Integer id, @RequestBody @Valid EducationLevelDto dto) {
        return educationLevelService.updateEducationLevelById(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        educationLevelService.delete(id);
    }
}
