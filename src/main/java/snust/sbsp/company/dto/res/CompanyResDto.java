package snust.sbsp.company.dto.res;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyResDto {
  private Long id;
  private String name;
  private String address;

  @Builder
  public CompanyResDto(Long id, String name, String address) {
    this.id = id;
    this.name = name;
    this.address = address;
  }
}
