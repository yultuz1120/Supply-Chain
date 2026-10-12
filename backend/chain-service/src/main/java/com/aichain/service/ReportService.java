package com.aichain.service;

import com.aichain.vo.OrderReportVO;
import com.aichain.vo.SalesTop10ReportVO;
import com.aichain.vo.TurnoverReportVO;
import com.aichain.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Yultuz
 */
public interface ReportService {
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    void exportBusinessDate(HttpServletResponse response);
}
