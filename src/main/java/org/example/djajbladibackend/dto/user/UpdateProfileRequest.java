package org.example.djajbladibackend.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 100)
    private String city;
}
