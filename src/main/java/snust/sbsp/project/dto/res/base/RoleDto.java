package snust.sbsp.project.dto.res.base;

import lombok.Getter;
import snust.sbsp.project.domain.type.ProjectRole;

@Getter
public class RoleDto {

	private final ProjectRole attr;

	private final String value;

	public RoleDto(ProjectRole projectRole) {
		this.attr = projectRole;
		this.value = projectRole.getValue();
	}
}
