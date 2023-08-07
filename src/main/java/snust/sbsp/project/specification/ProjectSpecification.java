package snust.sbsp.project.specification;

import org.springframework.data.jpa.domain.Specification;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;

public class ProjectSpecification {
	public static Specification<Project> equalCompanyId(Long companyId) {
		return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company").get("id"), companyId));
	}

	public static Specification<Project> equalName(String name) {
		return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), name));
	}

	public static Specification<Project> equalCtrType(CtrType ctrType) {
		return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ctrType"), ctrType));
	}

	public static Specification<Project> equalDetailCtrType(DetailCtrType detailCtrType) {
		return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("detailCtrType"), detailCtrType));
	}
}