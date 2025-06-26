package org.example.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponseDTO {
    private String accessToken;
    private String token;
    private String userId;
}
