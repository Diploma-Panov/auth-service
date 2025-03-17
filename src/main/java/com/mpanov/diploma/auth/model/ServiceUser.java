package com.mpanov.diploma.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ServiceUser")
@Table(name = "service_user", schema = "public")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServiceUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ServiceUserIds")
    @SequenceGenerator(
            name = "ServiceUserIds",
            sequenceName = "service_user_ids",
            allocationSize = 1,
            initialValue = 10001,
            schema = "public"
    )
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 63)
    @EqualsAndHashCode.Include
    private String firstname;

    @Column(length = 63)
    @EqualsAndHashCode.Include
    private String lastname;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String email;

    @Column(length = 15)
    @EqualsAndHashCode.Include
    private String phone;

    @Column(nullable = false, length = 4095)
    private String passwordHash;

    @Column(length = 511)
    @EqualsAndHashCode.Include
    private String profilePictureUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @EqualsAndHashCode.Include
    private UserType type;

    @OneToMany(
            mappedBy = "ownerUser",
            targetEntity = Organization.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    private Set<Organization> organizations = new HashSet<>();

    @OneToMany(
            mappedBy = "user",
            targetEntity = OrganizationMember.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    private Set<OrganizationMember> organizationMembers = new HashSet<>();

    public void addOrganization(Organization organization) {
        this.organizations.add(organization);
        organization.setOwnerUser(this);
    }

    public void addOrganizationMember(OrganizationMember organizationMember) {
        this.organizationMembers.add(organizationMember);
        organizationMember.setUser(this);
    }

}
