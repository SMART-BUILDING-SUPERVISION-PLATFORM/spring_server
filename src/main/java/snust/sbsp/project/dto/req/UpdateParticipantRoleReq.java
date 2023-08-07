package snust.sbsp.project.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateParticipantRoleReq {

	private Long projectId;

	private Long targetCrewId;

	private String role;
}
