package snust.sbsp.project.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import snust.sbsp.company.dto.res.base.CompanyDto;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.dto.res.base.ParticipantDto;
import snust.sbsp.project.dto.res.base.ProjectDto;

import java.util.List;

@Getter
@Builder
public class ProjectRes extends ProjectDto {

	@JsonIgnore
	private Project project;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final List<ParticipantDto> participantList;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final CompanyDto company;

	public ProjectRes(
		Project project,
		List<ParticipantDto> participantList,
		CompanyDto company
	) {
		super(project);
		this.participantList = participantList;
		this.company = company;
	}
}
