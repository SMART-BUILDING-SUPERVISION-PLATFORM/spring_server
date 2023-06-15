package snust.sbsp.project.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReq {

  private Long companyId;

  private String name;

  private LocalDate startDate;

  private LocalDate endDate;

  private String ctrType;

  private String detailCtrType;

  private String thumbnailUrl;

  private String floorPlanUrl;
}
