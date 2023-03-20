package snust.sbsp.company.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CompanyRes {
  private Long id;
  private String name;
  private String address;
}
