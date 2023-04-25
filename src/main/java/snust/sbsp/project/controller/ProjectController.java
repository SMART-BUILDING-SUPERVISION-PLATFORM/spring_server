package snust.sbsp.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.res.Response;
import snust.sbsp.common.util.SessionUtil;
import snust.sbsp.project.dto.req.ProjectReq;
import snust.sbsp.project.dto.res.base.ProjectDto;
import snust.sbsp.project.service.ProjectService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/project")
public class ProjectController {

  private final ProjectService projectService;

  private final SessionUtil sessionUtil;

  @PostMapping
  public ResponseEntity<?> newProject(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request,
    @RequestBody ProjectReq projectReq
  ) {
    Long crewId = sessionUtil.getInfo(jSessionId, request);
    projectService.createProject(projectReq, crewId);

    return Response.ok(HttpStatus.OK);
  }

  @GetMapping
  public ResponseEntity<List<ProjectDto>> getProjectList(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request,
    @RequestParam(required = false, value = "companyId") Long companyId,
    @RequestParam(required = false, value = "name") String name,
    @RequestParam(required = false, value = "ctrClass") String ctrClass,
    @RequestParam(required = false, value = "detailCtrClass") String detailCtrClass,
    @RequestParam(required = false, value = "onlyMine") Boolean onlyMine
  ) {
    Long crewId = sessionUtil.getInfo(jSessionId, request);
    List<ProjectDto> projectList;

    if (onlyMine)
      projectList = projectService.readMyProjectList(crewId);
    else
      projectList = projectService.readAllProjectList(companyId, name, ctrClass, detailCtrClass);


    return Response.ok(HttpStatus.OK, projectList);
  }
}
