package snust.sbsp.project.dto.res.base;

import lombok.Getter;
import snust.sbsp.project.domain.type.CtrType;

@Getter
public class CtrTypeDto {

  private final CtrType attr;

  private final String value;

  public CtrTypeDto(CtrType ctrType) {
    this.attr = ctrType;
    this.value = ctrType.getValue();
  }
}
