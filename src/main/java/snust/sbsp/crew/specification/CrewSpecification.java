package snust.sbsp.crew.specification;

import org.springframework.data.jpa.domain.Specification;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;

public class CrewSpecification {
  public static Specification<Crew> equalCompany(Company company) {
    return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company"), company));
  }

  public static Specification<Crew> equalName(String name) {
    return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), name));
  }

  public static Specification<Crew> equalRole(Role role) {
    return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role"), role));
  }

  public static Specification<Crew> equalIsPending(Boolean isPending) {
    return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isPending"), isPending));
  }
}