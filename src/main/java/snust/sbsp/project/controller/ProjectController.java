package snust.sbsp.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.res.Response;
import snust.sbsp.common.util.SessionUtil;
import snust.sbsp.project.dto.req.ProjectReq;
import snust.sbsp.project.dto.res.ProjectRes;
import snust.sbsp.project.service.ProjectService;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
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
  public ResponseEntity<List<ProjectRes>> getProjectList(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request,
    @PathParam("companyId") Long companyId,
    @PathParam("name") String name,
    @PathParam("ctrClass") String ctrClass,
    @PathParam("detailCtrClass") String detailCtrClass,
    @PathParam("only-me") String onlyMe
  ) {

    System.out.println(onlyMe);
    return Response.ok(HttpStatus.OK);
  }
}
