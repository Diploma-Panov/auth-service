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
@Entity(name = "Organization")
@Table(name = "organization", schema = "public")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OrganizationIds")
    @SequenceGenerator(
            name = "OrganizationIds",
            sequenceName = "organizationIds",
            allocationSize = 1,
            initialValue = 10001,
            schema = "public"
    )
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String organizationName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrganizationType type;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false,
            cascade = CascadeType.ALL,
            targetEntity = ServiceUser.class
    )
    @JoinColumn(
            name = "owner_user_id",
            foreignKey = @ForeignKey(name = "org_owner_user_fk")
    )
    @ToString.Exclude
    private ServiceUser ownerUser;

    @OneToMany(
            mappedBy = "organization",
            targetEntity = OrganizationMember.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    private Set<OrganizationMember> organizationMembers = new HashSet<>();

    public void addMember(OrganizationMember organizationMember) {
        organizationMember.setOrganization(this);
        this.organizationMembers.add(organizationMember);
    }

}
