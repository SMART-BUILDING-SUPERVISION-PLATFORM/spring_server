package snust.sbsp.crew.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.interceptor.Interceptor;
import snust.sbsp.common.res.Response;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.CrewRes;
import snust.sbsp.crew.service.CrewService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crew")
public class CrewController {

  private final CrewService crewService;

  @GetMapping
  public ResponseEntity<CrewRes> getInformation(
    @RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId
  ) {
    CrewRes crew = crewService.readCrew(currentCrewId);

    return Response.ok(HttpStatus.OK, crew);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CrewRes> getCrew(
    @PathVariable("id") Long id
  ) {
    CrewRes crew = crewService.readCrew(id);

    return Response.ok(HttpStatus.OK, crew);
  }

  @GetMapping("/admin-all")
  public ResponseEntity<List<CrewRes>> getAllCrewList(
    @RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
    @RequestParam(required = false, value = "companyId") Long companyId,
    @RequestParam(required = false, value = "isPending") Boolean isPending,
    @RequestParam(required = false, value = "role") Role role,
    @RequestParam(required = false, value = "name") String name
  ) {
    List<CrewRes> crewList = crewService.getAllCrewList(currentCrewId, companyId, isPending, role, name);

    return Response.ok(HttpStatus.OK, crewList);
  }

  @GetMapping("/admin-ca")
  public ResponseEntity<List<CrewRes>> getCompanyCrewList(
    @RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
    @RequestParam(required = false, value = "isPending") Boolean isPending,
    @RequestParam(required = false, value = "role") Role role,
    @RequestParam(required = false, value = "name") String name
  ) {
    List<CrewRes> crewList = crewService.readCompanyCrewList(currentCrewId, isPending, role, name);

    return Response.ok(HttpStatus.OK, crewList);
  }

  @PutMapping("/admin-ca/{id}")
  public ResponseEntity<?> togglePendingByCa(
    @RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
    @PathVariable("id") Long crewId
  ) {
    crewService.togglePendingByCa(currentCrewId, crewId);

    return Response.ok(HttpStatus.OK);
  }

  @PutMapping("/admin-sa/{id}")
  public ResponseEntity<?> togglePendingBySa(
    @RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
    @PathVariable("id") Long crewId
  ) {
    crewService.togglePendingBySa(currentCrewId, crewId);

    return Response.ok(HttpStatus.OK);
  }

  @DeleteMapping("/admin-ca/{id}")
  public ResponseEntity<?> deleteCompanyCrew(
    @RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
    @PathVariable("id") Long id
  ) {
    crewService.deleteCompanyCrew(currentCrewId, id);

    return Response.ok(HttpStatus.OK);
  }

  @DeleteMapping("/admin-sa/{id}")
  public ResponseEntity<?> deleteCrew(
    @RequestAttribute(Interceptor.CURRENT_CREW_ID) Long currentCrewId,
    @PathVariable("id") Long id
  ) {
    crewService.deleteCrew(currentCrewId, id);

    return Response.ok(HttpStatus.OK);
  }
}
