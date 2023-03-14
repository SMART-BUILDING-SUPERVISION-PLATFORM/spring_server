package snust.sbsp.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snust.sbsp.company.domain.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
