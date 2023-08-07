package snust.sbsp.project.domain;

import lombok.*;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.project.domain.type.NoteType;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor
@Entity(name = "note")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Note {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "note_type")
	@Enumerated(value = EnumType.STRING)
	private NoteType noteType;

	@Column(name = "content")
	private String content;

	@Column(name = "reply")
	private String reply;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "writer_id")
	private Crew writer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "replier_id")
	private Crew replier;

	public void update(
		Crew replier,
		String reply
	) {
		this.replier = replier;
		this.reply = reply;
	}
}

