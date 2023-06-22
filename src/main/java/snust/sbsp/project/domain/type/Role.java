package snust.sbsp.project.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Role {

	ADMIN("관리자"),
	MANAGER("프로젝트 관리자"),
	EDITABLE("참여 권한자"),
	READABLE("열람 권한자"),
	PENDING("참여승인 대기자");

	private final String value;

	public static Role from(String type) {
		return Arrays.stream(values())
			.filter(role -> role.value.equals(type))
			.findFirst()
			.orElseThrow(() -> new CustomCommonException(ErrorCode.BUSINESS_TYPE_INVALID));
	}
}
