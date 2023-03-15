package snust.sbsp.company.domain;

import lombok.*;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.project.domain.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "company")
public class Company {
  @OneToMany(mappedBy = "company")
  List<Crew> crewList = new ArrayList<>();
  @OneToMany(mappedBy = "company")
  List<Project> projectList = new ArrayList<>();
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "name")
  private String name;
  @Column(name = "address")
  private String address;
}
