package com.theokanning.openai.service.interceptor;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class QueryParamsInterceptor implements Interceptor {

    private final Map<String,String> queryMap;

    public QueryParamsInterceptor(Map<String, String> queryMap) {
        this.queryMap = queryMap;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // 添加查询参数
        final Request request = chain.request();
        final HttpUrl.Builder builder = request.url().newBuilder();
        Optional.ofNullable(queryMap).ifPresent(obj->{
            for (Map.Entry<String, String> stringEntry : obj.entrySet()) {
                builder.addQueryParameter(stringEntry.getKey(), stringEntry.getValue());
            }
        });
        // 执行请求
        return chain.proceed(request.newBuilder().url(builder.build()).build());
    }
}
