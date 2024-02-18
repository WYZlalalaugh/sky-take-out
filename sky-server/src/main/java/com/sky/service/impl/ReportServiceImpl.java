package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
