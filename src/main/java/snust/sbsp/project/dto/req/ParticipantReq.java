package snust.sbsp.project.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reactor.util.annotation.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantReq {

	private Long projectId;

	private Long targetCrewId;

	@Nullable
	private String role;
}
