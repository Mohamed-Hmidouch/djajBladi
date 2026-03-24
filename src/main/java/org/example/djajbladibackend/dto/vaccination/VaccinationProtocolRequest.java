package org.example.djajbladibackend.dto.vaccination;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VaccinationProtocolRequest {

    @NotBlank(message = "Strain is required")
    @Size(max = 100)
    private String strain;

    @NotBlank(message = "Vaccine name is required")
    @Size(max = 200)
    private String vaccineName;

    @NotNull(message = "Day of life is required")
    @Min(value = 1, message = "Day of life must be at least 1")
    private Integer dayOfLife;

    private String notes;
}
