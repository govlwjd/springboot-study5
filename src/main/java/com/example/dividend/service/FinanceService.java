package com.example.dividend.service;

import com.example.dividend.exception.impl.NoCompanyException;
import com.example.dividend.model.Company;
import com.example.dividend.model.Dividend;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import com.example.dividend.persist.repository.CompanyRepository;
import com.example.dividend.persist.repository.DividendRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public ScrapedResult getDividendByCompanyName(String companyName) {

        // 1. 회사명을 기준으로 회사 정보를 조회한다.
        CompanyEntity companyEntity = companyRepository.findByName(companyName)
                            .orElseThrow(() -> new NoCompanyException());

        // 2. 조회된 회사 id 로 배당금 정보를 조회한다.
        List<DividendEntity> dividendEntities = dividendRepository.findAllByCompanyId(companyEntity.getId());

        // 3. 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                                                .map(e -> Dividend.builder()
                                                        .date(e.getDate())
                                                        .dividend(e.getDividend())
                                                        .build())
                                                .collect(Collectors.toList());

        return new ScrapedResult(Company.builder()
                                        .ticker(companyEntity.getTicker())
                                        .name(companyEntity.getName())
                                        .build()
                                , dividends);
    }
}
