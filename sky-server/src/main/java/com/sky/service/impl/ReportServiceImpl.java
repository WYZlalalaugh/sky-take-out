package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.MarshalledObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> datelist=new ArrayList();
        List<Double> turnoverlist=new ArrayList();
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            datelist.add(begin);
        }
        //将list集合中的元素以逗号分隔转为string
        String join = StringUtils.join(datelist, ",");

        for (LocalDate date:datelist
             ) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
             turnover= turnover == null ? 0.0 : turnover;
            turnoverlist.add(turnover);
        }
        String join1 = StringUtils.join(turnoverlist, ",");
        return TurnoverReportVO.builder()
                .turnoverList(join1)
                .dateList(join)
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> datelist=new ArrayList();
        List<Integer> totalUserList=new ArrayList();
        List<Integer> newUserList=new ArrayList();
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            datelist.add(begin);
        }
        for (LocalDate date:datelist
             ) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap();

            map.put("end",endTime);
            Integer totalUser=userMapper.countByMap(map);
            totalUserList.add(totalUser);
            map.put("begin",beginTime);
            Integer newUser=userMapper.countByMap(map);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(datelist,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> datelist=new ArrayList();
        List<Integer> ordersCountList=new ArrayList();
        List<Integer> validOrderCountList=new ArrayList();
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            datelist.add(begin);
        }
        for (LocalDate date : datelist) {
            LocalDateTime beginTime=LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date,LocalTime.MAX);
            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            //查询每天订单总数
            Integer ordersCount=orderMapper.countByMap(map);
            ordersCountList.add(ordersCount);
            //查询每天有效订单总数
            map.put("status",Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
        }
       /* Integer totalOrderCount = ordersCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();*/
        int totalOrderCount=0;
        for (Integer i:ordersCountList
             ) {
            totalOrderCount=totalOrderCount+i;
        }
        int validOrderCount=0;
        for (Integer i:ordersCountList
        ) {
            validOrderCount=validOrderCount+i;
        }
        Double orderCompletionRate=0.0;
        if(totalOrderCount!=0) {
            orderCompletionRate = Double.valueOf(validOrderCount / totalOrderCount);
        }
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(datelist,","))
                .orderCountList(StringUtils.join(ordersCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime=LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(end,LocalTime.MAX);
        List<GoodsSalesDTO> salesTop = orderMapper.getSalesTop(beginTime, endTime);
        List<String> names=new ArrayList<>();
        List<Integer> numbers=new ArrayList<>();
        for (GoodsSalesDTO goods:salesTop
             ) {
            names.add(goods.getName());
            numbers.add(goods.getNumber());
        }
        String nameList = StringUtils.join(names, ",");
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response)  {
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel=new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间："+dateBegin+"至"+dateEnd);
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());
            for (int i = 0; i < 30; i++) {
                LocalDate date=dateBegin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                sheet.getRow(7+i).getCell(1).setCellValue(date.toString());
                sheet.getRow(7+i).getCell(2).setCellValue(businessDataVO.getTurnover());
                sheet.getRow(7+i).getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                sheet.getRow(7+i).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                sheet.getRow(7+i).getCell(5).setCellValue(businessDataVO.getUnitPrice());
                sheet.getRow(7+i).getCell(6).setCellValue(businessDataVO.getNewUsers());
            }
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
