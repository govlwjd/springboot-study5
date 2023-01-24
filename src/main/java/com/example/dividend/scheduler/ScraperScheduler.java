package com.example.dividend.scheduler;

import com.example.dividend.model.Company;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import com.example.dividend.persist.repository.CompanyRepository;
import com.example.dividend.persist.repository.DividendRepository;
import com.example.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;

    // 일정 주기마다 데이터를 스크래핑한다.
    @Scheduled(cron = "${scheduler.scrap.yahoo}") // 매일 정각마다 실행
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");
        // 저장된 회사 목록 조회
        List<CompanyEntity> companies = companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑한다.
        for (var company : companies) {
            ScrapedResult scrapedResult
                    = yahooFinanceScraper.scrap(Company.builder()
                                                    .ticker(company.getTicker())
                                                    .name(company.getName())
                                                    .build());

            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장한다.
            scrapedResult.getDividends().stream()
                    // Dividend 모델을 Dividend 엔티티로 매핑한다.
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 중복되지 않는 엘리먼트를 하나씩 Dividend 레파지토리에 삽입한다.
                    .forEach(e -> {
                        boolean exists = dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });
            // 스크래핑 대상 사이트 서버에 연속적으로 요청을 날리지 않도록 일시정지시킨다. -> 서버에 부하가 가지 않도록
            try {
                Thread.sleep(3000); // 3초
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
