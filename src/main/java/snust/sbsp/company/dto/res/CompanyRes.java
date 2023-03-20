package snust.sbsp.company.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import snust.sbsp.company.domain.Company;

@Getter
@Builder
@AllArgsConstructor
public class CompanyRes {

  private Long id;

  private String name;

  private String address;

  public CompanyRes(Company company) {
    this.id = company.getId();
    this.name = company.getName();
    this.address = company.getAddress();
  }
}
