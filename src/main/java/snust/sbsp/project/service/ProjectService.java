package snust.sbsp.project.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.project.dto.res.base.ProjectDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

  @Transactional(readOnly = true)
  public List<ProjectDto> readProjectList(Crew crew) {
    return crew.getParticipantList()
      .stream()
      .map(participant -> new ProjectDto(participant.getProject()))
      .collect(Collectors.toList());
  }
}
