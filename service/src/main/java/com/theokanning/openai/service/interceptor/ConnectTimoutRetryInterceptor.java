package com.theokanning.openai.service.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;


public class ConnectTimoutRetryInterceptor implements Interceptor {
    private int maxRetry;// 最大重试次数

    public ConnectTimoutRetryInterceptor(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return retry(chain, 0);
    }

    Response retry(Chain chain, int retryCent) {
        Request request = chain.request();
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            String err = e.getMessage();
            System.out.println("Retry request: retry = " + retryCent + ", error : "  + e.getMessage());
            if(err.contains("connect timed out") && maxRetry > retryCent){
                return retry(chain, retryCent + 1);
            }
        }
        if(response == null){
            throw new RuntimeException("Failed to connect host:" + request.url());
        }
        return response;
    }
}
