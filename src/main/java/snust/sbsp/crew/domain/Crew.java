package snust.sbsp.crew.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.type.Role;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Crew {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String email;

    private String password;

    private String name;

    private String phone;

    private String businessType;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Builder
    public Crew(String email, String password, String name, String phone, String businessType, Role role, Company company) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.businessType = businessType;
        this.role = role;
        this.company = company;
    }
}
