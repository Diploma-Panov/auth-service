package com.mpanov.diploma.auth.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mpanov.diploma.MemberRole;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrganizationAccessEntry implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    private Long organizationId;

    private Long[] allowedUrls;

    private Boolean allowedAllUrls;

    @Builder.Default
    private Set<MemberRole> roles = new HashSet<>();

}
