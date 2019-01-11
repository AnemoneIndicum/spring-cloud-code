package com.rui.cn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

/**
 * 程序主类
 *
 * @author zhangrl
 * @time 2019/1/9-11:23
 **/
@SpringBootApplication
public class CloudGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudGatewayApplication.class, args);
    }

    /**
     * 基本的转发
     * 当访问http://localhost:8080/jd
     * 转发到http://jd.com
     * @param builder
     * @return
     */
    @Bean("jd_route")
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        /// jd/路径下的所有请求替换掉 前缀，并且代理到 rui路径下
        return builder.routes()
                 .route( r -> r.path("/jd/**")
                         .filters(f -> f.rewritePath("/jd/(?<segment>.*)","/$\\{segment}"))
                .uri("http://jd.com:80/")
                .id("jd_route"))
                .build();
    }
    @Bean("retry_route")
    public RouteLocator routeRetry(RouteLocatorBuilder builder){
        /// 500错误是启用重试 且重试次数为2
        return builder.routes()
                .route( r -> r.path("/test/retry")
                        .filters(f -> f.retry(congfig -> congfig.setRetries(2).setStatuses(HttpStatus.INTERNAL_SERVER_ERROR))
                                .addRequestParameter("key","abc")
                                .addRequestParameter("count","2")
                        .stripPrefix(1))
                        .uri("http://localhost:7001")
                        .id("retry_route"))
                .build();
    }
    @Bean("hystrix_route")
    public RouteLocator routeHystrix(RouteLocatorBuilder builder){
        /// hystirix 过滤器 设置熔断
        return builder.routes()
                .route( r -> r.path("/test/hystrix")
                        .filters(f -> f.addRequestParameter("isSleep","true")
                        .hystrix(x -> x.setFallbackUri("forward:/fallback").setName("fallbackcmd ")))
                        .uri("http://localhost:7001")
                        .id("hystrix_route"))
                .build();
    }

}

