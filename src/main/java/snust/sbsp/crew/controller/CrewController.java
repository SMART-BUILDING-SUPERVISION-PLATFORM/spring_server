package snust.sbsp.crew.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.res.Response;
import snust.sbsp.common.util.SessionUtil;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.CrewRes;
import snust.sbsp.crew.service.CrewService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crew")
public class CrewController {
  private final SessionUtil sessionUtil;

  private final CrewService crewService;

  @GetMapping("/{id}")
  public ResponseEntity<CrewRes> getCrewList(
    @PathVariable("id") Long id
  ) {
    CrewRes crew = crewService.readCrew(id);

    return Response.ok(HttpStatus.OK, crew);
  }

  @GetMapping("/admin-all")
  public ResponseEntity<List<CrewRes>> getAllCrewList(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request,
    @RequestParam(required = false, value = "companyId") Long companyId,
    @RequestParam(required = false, value = "isPending") Boolean isPending,
    @RequestParam(required = false, value = "role") Role role,
    @RequestParam(required = false, value = "name") String name
  ) {
    Long crewId = sessionUtil.getInfo(jSessionId, request);
    List<CrewRes> crewList = crewService.getAllCrewList(crewId, companyId, isPending, role, name);

    return Response.ok(HttpStatus.OK, crewList);
  }

  @GetMapping("/admin-ca")
  public ResponseEntity<List<CrewRes>> getCompanyCrewList(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request,
    @RequestParam(required = false, value = "isPending") Boolean isPending,
    @RequestParam(required = false, value = "role") Role role,
    @RequestParam(required = false, value = "name") String name
  ) {
    Long crewId = sessionUtil.getInfo(jSessionId, request);
    List<CrewRes> crewList = crewService.readCompanyCrewList(crewId, isPending, role, name);

    return Response.ok(HttpStatus.OK, crewList);
  }

  @PutMapping("/admin-ca/{id}")
  public ResponseEntity<?> togglePendingByCa(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request,
    @PathVariable("id") Long crewId
  ) {
    crewService.togglePendingByCa(jSessionId, request, crewId);

    return Response.ok(HttpStatus.OK);
  }

  @PutMapping("/admin-sa/{id}")
  public ResponseEntity<?> togglePendingBySa(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request,
    @PathVariable("id") Long crewId
  ) {
    crewService.togglePendingBySa(jSessionId, request, crewId);

    return Response.ok(HttpStatus.OK);
  }
}
