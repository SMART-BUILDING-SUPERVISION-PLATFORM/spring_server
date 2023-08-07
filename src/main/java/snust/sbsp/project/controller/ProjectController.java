package snust.sbsp.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.common.interceptor.Interceptor;
import snust.sbsp.common.res.Response;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.service.CrewService;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.dto.req.PanoramaReq;
import snust.sbsp.project.dto.req.ProjectReq;
import snust.sbsp.project.dto.res.ProjectRes;
import snust.sbsp.project.dto.res.base.PanoramaDtoForPythonProject;
import snust.sbsp.project.service.ProjectService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/project")
public class ProjectController {

	private final ProjectService projectService;

	private final CrewService crewService;

	@GetMapping("/python/panorama/{id}")
	public ResponseEntity<List<PanoramaDtoForPythonProject>> getPanoramas(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId
	) {
		Crew currentCrew = crewService.readCrewById(currentCrewId);

		if (!currentCrew.getRole().equals(Role.SERVICE_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		Project project = projectService.readProjectById(projectId);

		List<PanoramaDtoForPythonProject> panoramaList = project.getPanoramaList()
			.stream()
			.map(PanoramaDtoForPythonProject::new)
			.collect(Collectors.toList());

		return Response.ok(HttpStatus.OK, panoramaList);
	}

	@PostMapping
	public ResponseEntity<?> newProject(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@RequestBody ProjectReq projectReq
	) {
		projectService.createProject(projectReq, currentCrewId);

		return Response.ok(HttpStatus.OK);
	}

	@PostMapping("/{id}")
	public ResponseEntity<?> addPanoramas(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId,
		@RequestBody PanoramaReq panoramaReq
	) {
		projectService.addPanoramas(projectId, currentCrewId, panoramaReq);

		return Response.ok(HttpStatus.OK);
	}


	@GetMapping("/{id}")
	public ResponseEntity<ProjectRes> getProject(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId
	) {
		ProjectRes projectRes = projectService.getProjectDtoByIdAndCrewId(currentCrewId, projectId);
		return Response.ok(HttpStatus.OK, projectRes);
	}

	@GetMapping
	public ResponseEntity<List<ProjectRes>> getProjectList(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@RequestParam(required = false, value = "companyId") Long companyId,
		@RequestParam(required = false, value = "name") String name,
		@RequestParam(required = false, value = "ctrType") String ctrType,
		@RequestParam(required = false, value = "detailCtrType") String detailCtrType,
		@RequestParam(required = false, value = "onlyMine") Boolean onlyMine,
		@RequestParam(required = false, value = "isPending") Boolean isPending
	) {
		List<ProjectRes> projectList;

		if (isPending)
			projectList = projectService.readMyPendingProjectList(currentCrewId, companyId, name, ctrType, detailCtrType);
		else {
			if (onlyMine)
				projectList = projectService.readMyProjectList(currentCrewId, companyId, name, ctrType, detailCtrType);
			else
				projectList = projectService.readExceptMyProjectList(currentCrewId, companyId, name, ctrType, detailCtrType);
		}

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
