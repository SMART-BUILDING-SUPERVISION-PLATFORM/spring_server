package snust.sbsp.project.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteParticipantReq {

	private Long projectId;

	private Long targetCrewId;
}
