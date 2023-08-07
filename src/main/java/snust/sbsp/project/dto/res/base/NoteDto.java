package snust.sbsp.project.dto.res.base;

import lombok.Getter;
import reactor.util.annotation.Nullable;
import snust.sbsp.crew.dto.res.base.CrewDto;
import snust.sbsp.project.domain.Note;

@Getter
public class NoteDto {

	private Long id;

	private String content;

	private String reply;

	private NoteTypeDto type;

	private CrewDto writer;

	@Nullable
	private CrewDto replier;

	public NoteDto(
		Note note,
		CrewDto writer,
		CrewDto replier
	) {
		this.id = note.getId();
		this.content = note.getContent();
		this.reply = note.getReply();
		this.writer = writer;
		this.replier = replier;
		this.type = new NoteTypeDto(note.getNoteType());
	}
}
