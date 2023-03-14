package snust.sbsp.crew.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.project.domain.Participant;

import javax.persistence.*;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "crew")
public class Crew {
  @JsonIgnore
  @OneToMany(mappedBy = "crew")
  List<Participant> participantList;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotNull
  @Column(name = "email")
  private String email;
  @NotNull
  @Column(name = "password")
  private String password;
  @NotNull
  @Column(name = "name")
  private String name;
  @NotNull
  @Column(name = "phone")
  private String phone;
  @NotNull
  @Column(name = "business_type")
  private String businessType;
  @NotNull
  @Column(name = "role")
  @Enumerated(value = EnumType.STRING)
  private Role role;
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @Builder
  public Crew(
    String email,
    String password,
    String name,
    String phone,
    String businessType,
    Role role,
    Company company
  ) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.phone = phone;
    this.businessType = businessType;
    this.role = role;
    this.company = company;
  }
}
