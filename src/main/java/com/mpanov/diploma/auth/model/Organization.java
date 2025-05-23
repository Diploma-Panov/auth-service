package com.mpanov.diploma.auth.model;

import com.mpanov.diploma.data.OrganizationScope;
import com.mpanov.diploma.data.OrganizationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Organization")
@Table(name = "organizations", schema = "public")
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

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String name;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String slug;

    @Column(length = 127)
    private String siteUrl;

    private String description;

    @Column(length = 512)
    private String organizationAvatarUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrganizationScope organizationScope;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrganizationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "creator_user_id",
            foreignKey = @ForeignKey(name = "org_owner_user_fk")
    )
    @ToString.Exclude
    private ServiceUser creatorUser;

    @Formula("(SELECT COUNT(*) FROM organization_members om WHERE om.organization_id = id)")
    private int membersCount;

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

    public void removeMember(OrganizationMember organizationMember) {
        organizationMember.setOrganization(null);
        this.organizationMembers.remove(organizationMember);
    }

    public void detach() {
        this.creatorUser.removeOrganization(this);
        for (OrganizationMember member : this.organizationMembers) {
            member.detach();
        }
        this.setOrganizationMembers(null);
    }

}
