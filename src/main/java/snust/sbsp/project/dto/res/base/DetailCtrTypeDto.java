package snust.sbsp.project.dto.res.base;

import lombok.Getter;
import snust.sbsp.project.domain.type.DetailCtrType;

@Getter
public class DetailCtrTypeDto {

  private final DetailCtrType attr;
  
  private final String value;

  public DetailCtrTypeDto(DetailCtrType detailCtrType) {
    this.attr = detailCtrType;
    this.value = detailCtrType.getValue();
  }
}
