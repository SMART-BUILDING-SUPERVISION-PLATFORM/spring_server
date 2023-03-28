package snust.sbsp.crew.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.res.Response;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.CrewRes;
import snust.sbsp.crew.service.CrewService;

import java.util.List;

@RestController
@RequestMapping("/api/crew")
@RequiredArgsConstructor
public class CrewController {
  private final CrewService crewService;

  @GetMapping("/{id}")
  public ResponseEntity<CrewRes> getCrewList(
    @PathVariable("id") Long id
  ) {
    CrewRes crew = crewService.readCrew(id);
    return Response.ok(HttpStatus.OK, crew);
  }

  @GetMapping
  public ResponseEntity<List<CrewRes>> getCrewList(
    @RequestParam(required = false, value = "companyId") Long companyId,
    @RequestParam(required = false, value = "isPending") Boolean isPending,
    @RequestParam(required = false, value = "role") Role role,
    @RequestParam(required = false, value = "name") String name
  ) {
    List<CrewRes> crewList = crewService.readCrewList(companyId, isPending, role, name);
    return Response.ok(HttpStatus.OK, crewList);
  }
}
