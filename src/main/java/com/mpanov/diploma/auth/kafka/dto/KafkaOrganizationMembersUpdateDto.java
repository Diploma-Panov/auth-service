package com.mpanov.diploma.auth.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaOrganizationMembersUpdateDto {
    private Long id;
    private Long organizationId;
    private String displayFirstname;
    private String displayLastname;
}
