package snust.sbsp.crew.domain;

import lombok.*;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.project.domain.Participant;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "crew")
public class Crew {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "name")
  private String name;

  @Column(name = "phone")
  private String phone;

  @Column(name = "business_type")
  private String businessType;

  @Column(name = "role")
  @Enumerated(value = EnumType.STRING)
  private Role role;

  @Column(name = "is_pending")
  private boolean isPending;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @OneToMany(mappedBy = "crew")
  private List<Participant> participantList = new ArrayList<>();
}
