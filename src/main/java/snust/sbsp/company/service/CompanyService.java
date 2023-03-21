package snust.sbsp.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.dto.res.CompanyRes;
import snust.sbsp.company.repository.CompanyRepository;

import javax.transaction.Transactional;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService {

  private final CompanyRepository companyRepository;

  public List<CompanyRes> findByName(String companyName) {
    return findCompanyListByPartOfName(companyName);
  }

  private List<CompanyRes> findCompanyListByPartOfName(String decodedCompanyName) {
    List<Company> companyList = companyRepository
      .findAll()
      .stream()
      .filter(
        company ->
          company
            .getName()
            .contains(decodedCompanyName)
      ).collect(Collectors.toList());
    return companyList.stream().map(CompanyRes::new).collect(Collectors.toList());
  }
}
