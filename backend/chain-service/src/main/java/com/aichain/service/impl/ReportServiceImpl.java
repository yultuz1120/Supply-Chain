package com.aichain.service.impl;

import com.aichain.dto.GoodsSalesDTO;
import com.aichain.entity.Orders;
import com.aichain.mapper.OrderMapper;
import com.aichain.mapper.UserMapper;
import com.aichain.service.ReportService;
import com.aichain.service.WorkspaceService;
import com.aichain.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yultuz
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String sdateList = StringUtils.join(dateList,",");

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date对应日期的营业额.从一天的0点到23：59：59
           LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
           LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //select sum(amont) from orders where order_time > ? and order_time < ? and status = 5
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);//计算出当天营业额
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        String sturnoverList = StringUtils.join(turnoverList,",");

        return TurnoverReportVO.builder()
                                .dateList(sdateList)
                                .turnoverList(sturnoverList)
                                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String sdateList = StringUtils.join(dateList,",");

        //select count(id) from user where create_time < ? and create_time > ?
        List<Integer> newUserList = new ArrayList<>();
        //select count(id) from user where create_time < ?
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUser = totalUser == null ? 0 : totalUser;
            totalUserList.add(totalUser);
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            newUser = newUser == null ? 0 : newUser;
            newUserList.add(newUser);
        }
        String stotalUserList = StringUtils.join(totalUserList,",");
        String snewuserList = StringUtils.join(newUserList,",");
        return UserReportVO.builder()
                .dateList(sdateList)
                .totalUserList(stotalUserList)
                .newUserList(snewuserList)
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String sdateList = StringUtils.join(dateList,",");

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //查询每天订单总数
            //select count(id) from orders where create_time < ? and create_time > ?
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(orderCount);
            // 和每天有效订单数
            //select count(id) from orders where create_time < ? and create_time > ? and satus = 5
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
        }
        //计算时间区间内得订单总数 遍历orderCountList 累加
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //时间区间内得有效订单数 同理
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCount!= 0){
            orderCompletionRate = (double)validOrderCount / totalOrderCount;
        }
        String sorderCountList = StringUtils.join(orderCountList,",");
        String svalidOrderCountList = StringUtils.join(validOrderCountList,",");

        return OrderReportVO.builder()
                .dateList(sdateList)
                .orderCountList(sorderCountList)
                .validOrderCountList(svalidOrderCountList)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        //select od.name,sum(od.number) number from order_detail od,orders o
        // where od.order_id = o.id and o.status = 5 and o.order_time > ? and o.order_time < ?
        //group by od.name order by number desc limit(0,10)
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop = orderMapper.getSalesTop(beginTime, endTime);
        //数据格式不匹配,转换格式
        List<String> names = salesTop.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String namelist = StringUtils.join(names, ",");
        List<Integer> numbers = salesTop.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberlist = StringUtils.join(numbers, ",");
        return SalesTop10ReportVO.builder()
                                    .nameList(namelist)
                                    .numberList(numberlist)
                                    .build();
    }

    @Override
    public void exportBusinessDate(HttpServletResponse response) {
        //1查询数据库，获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        //2通过POI写入到excel中,先读取模板，再写入
        //这里是反射的应用
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try{
            //基于模板创建一个新的excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);
            //填充数据
            XSSFSheet sheet = excel.getSheet("sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间" + dateBegin + "至" + dateEnd);
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            XSSFRow row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            //填充明细数据
            for(int i = 0; i < 30; i++){
                LocalDate date = dateBegin.plusDays(i);
                workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            //3通过输出流将excel下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.close();
            excel.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}


