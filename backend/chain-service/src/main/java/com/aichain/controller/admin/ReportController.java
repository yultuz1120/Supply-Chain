package com.aichain.controller.admin;

import com.aichain.result.Result;
import com.aichain.service.ReportService;
import com.aichain.vo.OrderReportVO;
import com.aichain.vo.SalesTop10ReportVO;
import com.aichain.vo.TurnoverReportVO;
import com.aichain.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * @author Yultuz
 */
@RestController
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {
    @Autowired
    private ReportService reportService;
    //统计营业额
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额数据统计：{},{}",begin,end);
        return Result.success(reportService.getTurnoverStatistics(begin,end));
    }

    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户数据统计：{},{}",begin,end);
        return Result.success(reportService.getUserStatistics(begin,end));
    }

    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单数据统计：{},{}",begin,end);
        return Result.success(reportService.getOrderStatistics(begin,end));
    }

    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("商品销量top10：{},{}",begin,end);
        return Result.success(reportService.getSalesTop10(begin,end));
    }

    @GetMapping("/export")
    //最后要以输出流的形式下载到客户端。所有要有response对象
    public void export(HttpServletResponse response) {
        reportService.exportBusinessDate(response);
    }
}


