package snust.sbsp.project.domain;

import com.sun.istack.NotNull;
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
  @NotNull
  @Column(name = "role")
  @Enumerated(value = EnumType.STRING)
  private Role role;
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "crew_id")
  private Crew crew;
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;
}
