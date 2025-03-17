package com.mpanov.diploma.auth.model;

import com.mpanov.diploma.MemberRole;
import io.hypersistence.utils.hibernate.type.array.LongArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "OrganizationMember")
@Table(name = "organization_member", schema = "public")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrganizationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OrganizationMemberIds")
    @SequenceGenerator(
            name = "OrganizationMemberIds",
            sequenceName = "organization_member_ids",
            allocationSize = 1,
            initialValue = 10001,
            schema = "public"
    )
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false,
            cascade = CascadeType.ALL,
            targetEntity = ServiceUser.class
    )
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "org_member_user_fk")
    )
    @ToString.Exclude
    private ServiceUser user;


    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false,
            cascade = CascadeType.ALL,
            targetEntity = Organization.class
    )
    @JoinColumn(
            name = "organization_id",
            foreignKey = @ForeignKey(name = "org_member_organization_fk")
    )
    @ToString.Exclude
    private Organization organization;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    private Set<MemberRole> roles;

    @Type(LongArrayType.class)
    @Column(nullable = false, columnDefinition = "BIGINT[]")
    @EqualsAndHashCode.Include
    private Long[] allowedUrls;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private Boolean allowedAllUrls;

}
