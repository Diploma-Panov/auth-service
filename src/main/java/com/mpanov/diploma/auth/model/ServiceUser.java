package com.mpanov.diploma.auth.model;

import com.mpanov.diploma.data.UserSystemRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ServiceUsers")
@Table(name = "users", schema = "public")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServiceUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ServiceUserIds")
    @SequenceGenerator(
            name = "ServiceUserIds",
            sequenceName = "user_ids",
            allocationSize = 1,
            initialValue = 10001,
            schema = "public"
    )
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, name = "first_name")
    @EqualsAndHashCode.Include
    private String firstname;

    @Column(name = "last_name")
    @EqualsAndHashCode.Include
    private String lastname;

    @Column
    private String companyName;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String email;

    @Column(nullable = false, length = 2048)
    private String passwordHash;

    @Column(length = 512)
    @EqualsAndHashCode.Include
    private String profilePictureUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @EqualsAndHashCode.Include
    private UserSystemRole systemRole;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime registrationDate;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime lastLoginDate;

    @OneToMany(
            mappedBy = "creatorUser",
            targetEntity = Organization.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER
    )
    @Builder.Default
    @ToString.Exclude
    private Set<Organization> organizations = new HashSet<>();

    @OneToMany(
            mappedBy = "memberUser",
            targetEntity = OrganizationMember.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    private Set<OrganizationMember> organizationMembers = new HashSet<>();

    public void addOrganization(Organization organization) {
        this.organizations.add(organization);
        organization.setCreatorUser(this);
    }

    public void addOrganizationMember(OrganizationMember organizationMember) {
        organizationMember.setMemberUser(this);
        this.organizationMembers.add(organizationMember);
    }

    public void removeOrganization(Organization organization) {
        organization.setCreatorUser(null);
        this.organizations.remove(organization);
    }

    public void removeMember(OrganizationMember organizationMember) {
        organizationMember.setMemberUser(null);
        this.organizationMembers.remove(organizationMember);
    }

}
