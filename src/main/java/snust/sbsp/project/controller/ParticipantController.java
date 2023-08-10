package snust.sbsp.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.interceptor.Interceptor;
import snust.sbsp.common.res.Response;
import snust.sbsp.project.dto.req.DeleteParticipantReq;
import snust.sbsp.project.dto.req.UpdateParticipantRoleReq;
import snust.sbsp.project.service.ParticipantService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/participant")
public class ParticipantController {

	private final ParticipantService participantService;

	// test complete
	@PostMapping("/{id}")
	public ResponseEntity<?> join(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId
	) {
		participantService.requestToJoin(currentCrewId, projectId);

		return Response.ok(HttpStatus.CREATED);
	}

	// test complete
	@PutMapping
	public ResponseEntity<?> update(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@RequestBody UpdateParticipantRoleReq updateParticipantRoleReq
	) {
		participantService.updateRole(currentCrewId, updateParticipantRoleReq);

		return Response.ok(HttpStatus.OK);
	}

	// test complete
	@DeleteMapping
	public ResponseEntity<?> delete(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@RequestParam("projectId") Long projectId,
		@RequestParam("targetCrewId") Long targetCrewId
	) {
		DeleteParticipantReq deleteParticipantReq = new DeleteParticipantReq(projectId, targetCrewId);

		participantService.deleteParticipant(currentCrewId, deleteParticipantReq);

		return Response.ok(HttpStatus.OK);
	}

}
