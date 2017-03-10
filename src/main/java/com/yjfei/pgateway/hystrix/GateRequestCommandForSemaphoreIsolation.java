package com.yjfei.pgateway.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class GateRequestCommandForSemaphoreIsolation extends HystrixCommand<HttpResponse> {

    HttpClient httpclient;
    HttpUriRequest httpUriRequest;
    HttpContext httpContext;

    public GateRequestCommandForSemaphoreIsolation(HttpClient httpclient, HttpUriRequest httpUriRequest, String commandGroup, String commandKey) {
        this(httpclient, httpUriRequest, null, commandGroup, commandKey);
    }

    public GateRequestCommandForSemaphoreIsolation(HttpClient httpclient, HttpUriRequest httpUriRequest, HttpContext httpContext, String commandGroup, String commandKey) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andCommandPropertiesDefaults(
                        // we want to default to semaphore-isolation since this wraps
                        // 2 others commands that are already thread isolated
                        HystrixCommandProperties.Setter()
                                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                ));

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
