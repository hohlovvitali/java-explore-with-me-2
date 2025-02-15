package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewCompilationDto {
    List<Long> events;
    @Builder.Default
    Boolean pinned = false;
    @NotBlank
    @Size(min = 1, max = 50)
    String title;
}