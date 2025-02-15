package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserShortDto {
    @NotNull
    private Long id;

    @NotBlank
    private String name;
}