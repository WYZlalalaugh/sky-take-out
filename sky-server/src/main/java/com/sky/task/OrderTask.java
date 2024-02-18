package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    /*
    * 处理超时订单*/
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeOutOrder(){
        List<Orders> ordersList = orderMapper.getOrdersByStatusAndTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(ordersList !=null&&ordersList.size()>0){
           for (Orders orders:ordersList){
               orders.setStatus(Orders.CANCELLED);
               orders.setCancelReason("订单超时");
               orders.setCancelTime(LocalDateTime.now());
               orderMapper.update(orders);
           }
        }
    }
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder(){
        List<Orders> ordersList=orderMapper.getOrdersByStatusAndTimeLT(Orders.DELIVERY_IN_PROGRESS,LocalDateTime.now().plusHours(-1));
        if(ordersList !=null&&ordersList.size()>0){
            for (Orders orders:ordersList){
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
