package snust.sbsp.project.specification;

import org.springframework.data.jpa.domain.Specification;
import snust.sbsp.company.domain.Company;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;

public class ProjectSpecification {
  public static Specification<Project> equalCompany(Company company) {
    return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company"), company));
  }

  public static Specification<Project> equalName(String name) {
    return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), name));
  }

  public static Specification<Project> equalCtrClass(CtrType ctrClass) {
    return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ctrClass"), ctrClass));
  }

  public static Specification<Project> equalDetailCtrClass(DetailCtrType detailCtrClass) {
    return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("detailCtrClass"), detailCtrClass));
  }
}