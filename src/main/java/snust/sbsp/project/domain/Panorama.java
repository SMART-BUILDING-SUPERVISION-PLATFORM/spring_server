package snust.sbsp.project.domain;

import lombok.*;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor
@Entity(name = "panorama")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Panorama {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Lob
	@Column(name = "src", columnDefinition = "text")
	private String src;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	public Panorama(
		String src,
		Project project
	) {
		this.src = src;
		this.project = project;
	}
}
