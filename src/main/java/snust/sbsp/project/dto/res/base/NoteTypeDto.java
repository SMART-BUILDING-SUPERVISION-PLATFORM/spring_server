package snust.sbsp.project.dto.res.base;

import lombok.Getter;
import snust.sbsp.project.domain.type.NoteType;

@Getter
public class NoteTypeDto {
	private final NoteType attr;

	private final String value;

	public NoteTypeDto(NoteType noteType) {
		this.attr = noteType;
		this.value = noteType.getValue();
	}
}
