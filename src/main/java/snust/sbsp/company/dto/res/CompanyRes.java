package snust.sbsp.company.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.dto.res.base.CompanyDto;
import snust.sbsp.crew.dto.res.base.CrewDto;
import snust.sbsp.project.dto.res.base.ProjectDto;

import java.util.List;

@Getter
@Builder
public class CompanyRes extends CompanyDto {

  @JsonIgnore
  private Company company;
  private List<CrewDto> crewList;
  private List<ProjectDto> projectList;

  public CompanyRes(
    Company company,
    List<CrewDto> crewList,
    List<ProjectDto> projectList
  ) {
    super(company);
    this.crewList = crewList;
    this.projectList = projectList;
  }
}

