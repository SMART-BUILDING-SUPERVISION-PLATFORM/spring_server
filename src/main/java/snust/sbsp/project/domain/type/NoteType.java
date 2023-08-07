package snust.sbsp.project.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum NoteType {

	CAUTION("caution"),
	SUPPORT("support");

	private final String value;

	public static NoteType toEnum(String type) {
		return Arrays.stream(values())
			.filter(noteType -> noteType.value.equals(type))
			.findFirst()
			.orElseThrow(() -> new CustomCommonException(ErrorCode.BUSINESS_TYPE_INVALID));
	}
}
