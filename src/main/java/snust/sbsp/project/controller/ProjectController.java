package snust.sbsp.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.interceptor.Interceptor;
import snust.sbsp.common.res.Response;
import snust.sbsp.project.dto.req.ProjectReq;
import snust.sbsp.project.dto.res.base.ProjectDto;
import snust.sbsp.project.service.ProjectService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/project")
public class ProjectController {

	private final ProjectService projectService;

	@PostMapping
	public ResponseEntity<?> newProject(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@RequestBody ProjectReq projectReq
	) {
		projectService.createProject(projectReq, currentCrewId);

		return Response.ok(HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<List<ProjectDto>> getProjectList(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@RequestParam(required = false, value = "companyId") Long companyId,
		@RequestParam(required = false, value = "name") String name,
		@RequestParam(required = false, value = "ctrClass") String ctrClass,
		@RequestParam(required = false, value = "detailCtrClass") String detailCtrClass,
		@RequestParam(required = false, value = "onlyMine") Boolean onlyMine
	) {
		List<ProjectDto> projectList;

		if (onlyMine)
			projectList = projectService.readMyProjectList(currentCrewId);
		else
			projectList = projectService.readExceptMyProjectList(currentCrewId, companyId, name, ctrClass, detailCtrClass);


		return Response.ok(HttpStatus.OK, projectList);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateProject(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long id,
		@RequestBody ProjectReq projectReq
	) {
		projectService.updateProject(id, projectReq, currentCrewId);

		return Response.ok(HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteProject(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long id
	) {
		projectService.deleteProject(id, currentCrewId);

		return Response.ok(HttpStatus.OK);
	}
}
