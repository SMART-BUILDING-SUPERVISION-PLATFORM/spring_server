package snust.sbsp.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.dto.res.CompanyRes;
import snust.sbsp.company.repository.CompanyRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

	private final CompanyRepository companyRepository;

	@Transactional(readOnly = true)
	public List<CompanyRes> readCompany(String companyName) {
		// DB에 저장된 모든 회사 조회
		List<Company> companyList = companyRepository.findAll();

		// companyName == null이면 모든 회사 조회
		if (companyName != null) {
			companyList =
				companyList
					.stream()
					.filter(company ->
						company
							.getName()
							.toLowerCase()
							.contains(companyName.toLowerCase())
					).collect(Collectors.toList());
		}

		// companyName != null이면 하나의 회사만 조회.
		return companyList
			.stream()
			.map(company ->
				CompanyRes
					.builder()
					.company(company)
					.build()
			).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Company findById(Long companyId) {
		// CompanyID로 회사 조회.
		return companyRepository.findById(companyId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.COMPANY_NOT_FOUND));
	}
}
