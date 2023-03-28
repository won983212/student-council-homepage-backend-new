package com.dku.council.infra.dku.service;

import com.dku.council.global.config.webclient.ChromeAgentWebClient;
import com.dku.council.global.error.exception.UnexpectedResponseException;
import com.dku.council.infra.dku.exception.DkuFailedCrawlingException;
import com.dku.council.infra.dku.model.DkuAuth;
import com.dku.council.infra.dku.model.StudentDuesStatus;
import com.dku.council.infra.dku.model.StudentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.YearMonth;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DkuCrawlerService {

    @ChromeAgentWebClient
    private final WebClient webClient;

    @Value("${dku.student-info.info-api-path}")
    private final String studentInfoApiPath;

    @Value("${dku.student-info.fee-api-path}")
    private final String feeInfoApiPath;


    /**
     * 학생 정보를 크롤링해옵니다.
     *
     * @param auth 인증 토큰
     * @return 학생 정보
     */
    public StudentInfo crawlStudentInfo(DkuAuth auth) {
        String html = request(auth, studentInfoApiPath);
        return parseStudentInfoHtml(html);
    }

    /**
     * 학생회비 납부 정보를 크롤링해옵니다.
     *
     * @param auth 인증 토큰
     * @return 학생 정보
     */
    public StudentDuesStatus crawlStudentDues(DkuAuth auth, YearMonth yearMonth) {
        String html = request(auth, feeInfoApiPath);
        return parseDuesStatusHtml(html, yearMonth);
    }

    private String request(DkuAuth auth, String uri) {
        String result;
        try {
            result = webClient.post()
                    .uri(uri)
                    .cookies(auth.authCookies())
                    .header("Referer", "https://webinfo.dankook.ac.kr/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Throwable t) {
            throw new DkuFailedCrawlingException(t);
        }
        return result;
    }

    private StudentDuesStatus parseDuesStatusHtml(String html, YearMonth yearMonth) {
        Document doc = Jsoup.parse(html);

        try {
            Element table = doc.getElementById("tbl_semList");
            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()) {
                throw new DkuFailedCrawlingException(new UnexpectedResponseException("table is empty"));
            }

            for (int i = 0; i < rows.size(); i++) {
                Element row = rows.get(0);
                Elements cols = row.select("td");
                if (cols.size() != 6) {
                    throw new DkuFailedCrawlingException(new UnexpectedResponseException("table column size is not 6"));
                }

                if (!isThisPaidDues(yearMonth, cols)) continue;

                String needFees = cols.get(4).text();
                String paidFees = cols.get(5).text();

                if (needFees.equals(paidFees)) {
                    return StudentDuesStatus.PAID;
                } else {
                    return StudentDuesStatus.NOT_PAID;
                }
            }

            return StudentDuesStatus.NOT_PAID;
        } catch (NullPointerException e) {
            throw new DkuFailedCrawlingException(e);
        }
    }

    private static boolean isThisPaidDues(YearMonth yearMonth, Elements cols) {
        String year = cols.get(1).text();
        String semester = cols.get(2).text();
        String type = cols.get(3).text();

        if (!type.equals("학생회비")) {
            return false;
        }

        if (!String.valueOf(yearMonth.getYear()).equals(year)) {
            return false;
        }

        int monthValue = yearMonth.getMonthValue();
        if (monthValue >= 2 && monthValue < 9) {
            return semester.equals("1");
        } else return semester.equals("2");
    }

    private StudentInfo parseStudentInfoHtml(String html) {
        Document doc = Jsoup.parse(html);

        String studentName = getElementValueOrThrow(doc, "nm");
        String studentId = getElementValueOrThrow(doc, "stuid");
        String studentState = getElementValueOrThrow(doc, "scregStaNm");

        String major, department = "";
        int yearOfAdmission;

        try {
            // 사회과학대학 정치외교학과
            major = getElementValueOrThrow(doc, "pstnOrgzNm");
            major = major.trim();

            int spaceIdx = major.lastIndexOf(' ');
            if (spaceIdx >= 0) {
                department = major.substring(0, spaceIdx);
                major = major.substring(spaceIdx + 1);
            }

            String etrsYy = getElementValueOrThrow(doc, "etrsYy");
            yearOfAdmission = Integer.parseInt(etrsYy);
        } catch (Throwable t) {
            throw new DkuFailedCrawlingException(t);
        }

        return new StudentInfo(studentName, studentId, yearOfAdmission, studentState, major, department);
    }

    private String getElementValueOrThrow(Document doc, String id) {
        String value = Optional.ofNullable(doc.getElementById(id))
                .map(Element::val)
                .orElseThrow(() -> new DkuFailedCrawlingException(new NullPointerException(id)));
        if (value.isBlank()) {
            throw new DkuFailedCrawlingException(new NullPointerException(id));
        }
        return value;
    }
}
