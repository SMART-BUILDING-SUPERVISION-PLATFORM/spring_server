package snust.sbsp.company.dto.res.base;

import lombok.Getter;
import snust.sbsp.company.domain.Company;

@Getter
public class CompanyDto {
  private Long id;
  private String name;
  private String address;

  public CompanyDto(Company company) {
    this.id = company.getId();
    this.name = company.getName();
    this.address = company.getAddress();
  }
}
