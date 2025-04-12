package com.mpanov.diploma.auth.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaUserUpdateDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private List<KafkaOrganizationUpdateDto> organizationsCreatedByUser;
    private List<KafkaOrganizationMembersUpdateDto> members;
}
