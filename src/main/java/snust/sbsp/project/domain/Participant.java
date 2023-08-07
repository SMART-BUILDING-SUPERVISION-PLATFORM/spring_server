package snust.sbsp.project.domain;

import lombok.*;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.project.domain.type.ProjectRole;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor
@Entity(name = "participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "project_role")
	@Enumerated(value = EnumType.STRING)
	private ProjectRole projectRole;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "crew_id")
	private Crew crew;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	public void update(ProjectRole projectRole) {
		this.projectRole = projectRole;
	}
}
