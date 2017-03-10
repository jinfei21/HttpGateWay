package com.yjfei.pgateway.hystrix;


import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;

public class GateRequestCommandForThreadIsolation extends HystrixCommand<HttpResponse> {

    HttpClient httpclient;
    HttpUriRequest httpUriRequest;
    HttpContext httpContext;

    public GateRequestCommandForThreadIsolation(HttpClient httpclient, HttpUriRequest httpUriRequest, String commandGroup, String commandKey) {
        this(httpclient, httpUriRequest, null,commandGroup, commandKey);
    }
    public GateRequestCommandForThreadIsolation(HttpClient httpclient, HttpUriRequest httpUriRequest, HttpContext httpContext, String commandGroup, String commandKey) {
        this(httpclient, httpUriRequest, httpContext,commandGroup, commandKey, GateCommandHelper.getThreadPoolKey(commandGroup,commandKey));
    }

    public GateRequestCommandForThreadIsolation(HttpClient httpclient, HttpUriRequest httpUriRequest, HttpContext httpContext, String commandGroup, String commandKey, String threadPoolKey) {

        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                )
        );
        this.httpclient = httpclient;
        this.httpUriRequest = httpUriRequest;
        this.httpContext = httpContext;
    }

    @Override
    protected HttpResponse run() throws Exception {
        try {
            return forward();
        } catch (IOException e) {
            throw e;
        }
    }

    HttpResponse forward() throws IOException {
        return httpclient.execute(httpUriRequest, httpContext);
    }
}
