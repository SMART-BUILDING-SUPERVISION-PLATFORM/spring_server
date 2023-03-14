package snust.sbsp.company.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.project.domain.Project;

import javax.persistence.*;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "company")
public class Company {
  @JsonIgnore
  @OneToMany(mappedBy = "company")
  List<Crew> crewList;
  @JsonIgnore
  @OneToMany(mappedBy = "company")
  List<Project> projectList;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotNull
  @Column(name = "name")
  private String name;
  @NotNull
  @Column(name = "address")
  private String address;

  @Builder
  public Company(String name, String address) {
    this.name = name;
    this.address = address;
  }
}
