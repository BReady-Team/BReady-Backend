package com.bready.server.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateBioRequest(
        @Size(max = 255)
        String bio
) {
}
