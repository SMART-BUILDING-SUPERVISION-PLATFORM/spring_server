package snust.sbsp.company.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.dto.res.CompanyResDto;
import snust.sbsp.company.repository.CompanyRepository;

import javax.transaction.Transactional;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyService {
  private final CompanyRepository companyRepository;

  @Autowired
  public CompanyService(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
  }

  public Optional<Company> findById(Long companyId) {
    return companyRepository.findById(companyId);
  }

  public List<CompanyResDto> findByName(String companyName) {
    List<Company> companyList = companyRepository
      .findAll()
      .stream()
      .filter(
        company ->
          company
            .getName()
            .contains(URLDecoder.decode(companyName)))
      .collect(Collectors.toList());
    return companyList.stream().map(company ->
        CompanyResDto.builder()
          .id(company.getId())
          .name(company.getName())
          .address(company.getAddress())
          .build())
      .collect(Collectors.toList());
  }
}
