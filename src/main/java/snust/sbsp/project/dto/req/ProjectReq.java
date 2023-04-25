package snust.sbsp.project.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProjectReq {

  private final Long companyId;

  private final String name;

  private final LocalDate startDate;

  private final LocalDate endDate;

  private final String ctrType;

  private final String detailCtrType;

  private final String thumbnailUrl;

  private final String floorPlanUrl;
}
