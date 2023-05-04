package snust.sbsp.crew.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;

@Component
public class CrewSpecification {
    public static Specification<Crew> equalCompanyId(Long companyId) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company").get("id"), companyId));
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

    public Specification<Crew> getSpecification(
            String name,
            Role role,
            Boolean isPending,
            Long companyId) {
        Specification<Crew> specification = ((root, query, criteriaBuilder) -> null);
        if (name != null)
            specification = specification.and(CrewSpecification.equalName(name));
        if (role != null)
            specification = specification.and(CrewSpecification.equalRole(role));
        if (isPending != null)
            specification = specification.and(CrewSpecification.equalIsPending(isPending));
        if (companyId != null)
            specification = specification.and(CrewSpecification.equalCompanyId(companyId));

        return specification;
    }
}