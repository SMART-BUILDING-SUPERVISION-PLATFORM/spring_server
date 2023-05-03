package snust.sbsp.common.interceptor.currentCrew;

import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;

@Getter
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentCrew {

  private final Long crewId;
  private final String name;
  private final Role role;
  private final Company company;

  public CurrentCrew(Crew currentCrew) {
    this.crewId = currentCrew.getId();
    this.name = currentCrew.getName();
    this.role = currentCrew.getRole();
    this.company = currentCrew.getCompany();
  }
}