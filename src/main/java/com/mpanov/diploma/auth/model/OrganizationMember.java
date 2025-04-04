package com.mpanov.diploma.auth.model;

import com.mpanov.diploma.data.MemberRole;
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
@Table(name = "organization_members", schema = "public")
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
            fetch = FetchType.EAGER,
            targetEntity = ServiceUser.class
    )
    @JoinColumn(
            name = "member_user_id",
            foreignKey = @ForeignKey(name = "org_member_user_fk")
    )
    @ToString.Exclude
    private ServiceUser memberUser;


    @ManyToOne(
            fetch = FetchType.EAGER,
            targetEntity = Organization.class
    )
    @JoinColumn(
            name = "organization_id",
            foreignKey = @ForeignKey(name = "org_member_organization_fk")
    )
    @ToString.Exclude
    private Organization organization;

    @Column(name = "display_firstname")
    private String displayFirstname;

    @Column(name = "display_lastname")
    private String displayLastname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    private Set<MemberRole> roles;

    @Type(LongArrayType.class)
    @Column(nullable = false, columnDefinition = "BIGINT[]")
    @EqualsAndHashCode.Include
    private Long[] memberUrls;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private Boolean allowedAllUrls;

    public void detach() {
        this.getMemberUser().removeMember(this);
        this.getOrganization().removeMember(this);
    }

}
