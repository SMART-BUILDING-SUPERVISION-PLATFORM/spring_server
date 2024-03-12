package snust.sbsp.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.interceptor.Interceptor;
import snust.sbsp.common.res.Response;
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

	// test complete
	@PostMapping
	public ResponseEntity<?> newProject(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@RequestBody ProjectReq projectReq
	) {
		projectService.createProject(projectReq, currentCrewId);

		return Response.ok(HttpStatus.OK);
	}

	// test complete
	@PostMapping("/{id}")
	public ResponseEntity<?> addPanoramas(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId,
		@RequestBody PanoramaReq panoramaReq
	) {
		projectService.addPanoramas(projectId, currentCrewId, panoramaReq);

		return Response.ok(HttpStatus.OK);
	}


	// test complete
	@GetMapping("/{id}")
	public ResponseEntity<ProjectRes> getProject(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId
	) {
		ProjectRes projectRes = projectService.getProjectDtoByIdAndCrewId(currentCrewId, projectId);
		return Response.ok(HttpStatus.OK, projectRes);
	}

	// test complete
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
			// 참여승인 대기중인 나의 프로젝트만 조회.
			projectList = projectService.readMyPendingProjectList(currentCrewId, companyId, name, ctrType, detailCtrType);
		else {
			if (onlyMine)
				// 내가 참여중(대기X)인 프로젝트만 조회.
				projectList = projectService.readMyProjectList(currentCrewId, companyId, name, ctrType, detailCtrType);
			else
				// 내가 참여하는(대기 포함) 프로젝트만 조회.
				projectList = projectService.readExceptMyProjectList(currentCrewId, companyId, name, ctrType, detailCtrType);
		}

		return Response.ok(HttpStatus.OK, projectList);
	}

	// test complete
	@PutMapping("/{id}")
	public ResponseEntity<?> updateProject(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId,
		@RequestBody ProjectReq projectReq
	) {
		projectService.updateProject(projectId, projectReq, currentCrewId);

		return Response.ok(HttpStatus.OK);
	}

	// test complete
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteProject(
		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId
	) {
		projectService.deleteProject(projectId, currentCrewId);

		return Response.ok(HttpStatus.OK);
	}

	// test complete (not for this service. only for service admin.)
	@GetMapping("/python/panorama/{id}")
	public ResponseEntity<List<PanoramaDtoForPythonProject>> getPanoramas(
//		@RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
		@PathVariable("id") Long projectId
	) {
//		Crew currentCrew = crewService.readCrewById(currentCrewId);
//		if (!currentCrew.getRole().equals(Role.SERVICE_ADMIN))
//			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		System.out.println(projectId);
		Project project = projectService.readProjectById(projectId);

		List<PanoramaDtoForPythonProject> panoramaList = project.getPanoramaList()
			.stream()
			.map(PanoramaDtoForPythonProject::new)
			.collect(Collectors.toList());

		return Response.ok(HttpStatus.OK, panoramaList);
	}
}
