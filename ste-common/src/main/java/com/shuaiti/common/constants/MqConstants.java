package com.shuaiti.common.constants;

public interface MqConstants {
    interface Exchange{
        /*订单有关的交换机*/
        String ORDER_EXCHANGE = "order.topic";
    }
    interface Queue {
        String ERROR_QUEUE_TEMPLATE = "error.{}.queue";
    }
    interface Key{
        /*订单有关的RoutingKey*/
        String ORDER_PAY_KEY = "order.pay";
        String ORDER_REFUND_KEY = "order.refund";
    }
}
