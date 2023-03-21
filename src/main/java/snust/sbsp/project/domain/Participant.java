package snust.sbsp.project.domain;

import lombok.*;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.project.domain.type.Role;

import javax.persistence.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "participant")
public class Participant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "role")
  @Enumerated(value = EnumType.STRING)
  private Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "crew_id")
  private Crew crew;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;
}
