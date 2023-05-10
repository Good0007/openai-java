package com.theokanning.openai.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.theokanning.openai.*;
import com.theokanning.openai.conversation.chat.ChatConversationRequest;
import com.theokanning.openai.conversation.chat.ConversationMessage;
import com.theokanning.openai.conversation.chat.chunk.ChatConversationChunk;
import com.theokanning.openai.service.interceptor.AuthenticationInterceptor;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.*;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class OpenChatgptService {

    public static String BASE_URL = "https://api.pawan.krd/";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final ObjectMapper errorMapper = defaultObjectMapper();

    private final OpenChatgptApi api;
    private final ExecutorService executorService;

    public OpenChatgptService(final String token, final String api) {
        this(token, DEFAULT_TIMEOUT);
        BASE_URL = api;
    }

    public static void main(String[] args) {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik1UaEVOVUpHTkVNMVFURTRNMEZCTWpkQ05UZzVNRFUxUlRVd1FVSkRNRU13UmtGRVFrRXpSZyJ9.eyJodHRwczovL2FwaS5vcGVuYWkuY29tL3Byb2ZpbGUiOnsiZW1haWwiOiJ0eWRpY2dwdDFAeWFob28uY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWV9LCJodHRwczovL2FwaS5vcGVuYWkuY29tL2F1dGgiOnsidXNlcl9pZCI6InVzZXIteGFBVFNOVDBCOW1PNHhTVXVEc056aVg3In0sImlzcyI6Imh0dHBzOi8vYXV0aDAub3BlbmFpLmNvbS8iLCJzdWIiOiJhdXRoMHw2NDIxNTUzZjM5NTkzZDg0ZTQ2NTcxZTYiLCJhdWQiOlsiaHR0cHM6Ly9hcGkub3BlbmFpLmNvbS92MSIsImh0dHBzOi8vb3BlbmFpLm9wZW5haS5hdXRoMGFwcC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNjgwMTY0NzU1LCJleHAiOjE2ODEzNzQzNTUsImF6cCI6IlRkSkljYmUxNldvVEh0Tjk1bnl5d2g1RTR5T282SXRHIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCBtb2RlbC5yZWFkIG1vZGVsLnJlcXVlc3Qgb3JnYW5pemF0aW9uLnJlYWQgb2ZmbGluZV9hY2Nlc3MifQ.2RJrrRtgUpgeF_uUqaDTdCFgmTsdnMiZEntvx7vEl7fQW-pToJj7AEvFUJOOjoX_lDmkGd5VESx3I2CmKwGgjwgFR0e5y11vIoXF4PLsGan-jtsAqmf0SHZF47vomYuCcRhCY1O3y7kquegqMt7GJrpxEEWIvQh8R1oWCmJJbt7N5rdo9l4ffDz4GXT_iyg9QnoU0rs-MHaq5olKMf961HaKT3Ma4uVPNESjfPTLGKvmibv5ndKGM0rIEInXmCWvar1bo3ZtKNiyYbdy5HOPQM8-OB-jHScbCKQ__ycfuwwXvCaXVzfeObe6KRNx73ShhDuPY1nvUpnyjl8-x7LAXQ";
        OpenChatgptService service = new OpenChatgptService(token);
        ConversationMessage message = new ConversationMessage("你好");
        ChatConversationRequest request = new ChatConversationRequest("",message);
        service.streamChatConversation(request).blockingForEach(obj->{
            System.out.println(obj.toString());
        });
    }
    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     */
    public OpenChatgptService(final String token) {
        this(token, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token   OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     * @param timeout http read timeout, Duration.ZERO means no timeout
     */
    public OpenChatgptService(final String token, final Duration timeout) {
        this(defaultClient(token, timeout));
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param client OkHttpClient to be used for api calls
     */
    public OpenChatgptService(OkHttpClient client){
        this(buildApi(client), client.dispatcher().executorService());
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi.
     * The ExecutoryService must be the one you get from the client you created the api with
     * otherwise shutdownExecutor() won't work. Use this if you need more customization.
     *
     * @param api OpenAiApi instance to use for all methods
     * @param executorService the ExecutorService from client.dispatcher().executorService()
     */
    public OpenChatgptService(final OpenChatgptApi api, final ExecutorService executorService) {
        this.api = api;
        this.executorService = executorService;
    }

    public Flowable<byte[]> streamChatConversationBytes(ChatConversationRequest request) {
		return stream(api.createChatConversationStream(request), true).map(sse -> sse.toBytes());
	}

	public Flowable<ChatConversationChunk> streamChatConversation(ChatConversationRequest request) {
		return stream(api.createChatConversationStream(request), ChatConversationChunk.class);
	}


    /**
     * Calls the Open AI api, returns the response, and parses error messages if the request fails
     */
    public static <T> T execute(Single<T> apiCall) {
        try {
            return apiCall.blockingGet();
        } catch (HttpException e) {
            try {
                if (e.response() == null || e.response().errorBody() == null) {
                    throw e;
                }
                String errorBody = e.response().errorBody().string();

                OpenAiError error = errorMapper.readValue(errorBody, OpenAiError.class);
                throw new OpenAiHttpException(error, e, e.code());
            } catch (IOException ex) {
                // couldn't parse OpenAI error
                throw e;
            }
        }
    }

    /**
     * Calls the Open AI api and returns a Flowable of SSE for streaming
     * omitting the last message.
     * 
     * @param apiCall The api call
     */
    public static Flowable<SSE> stream(Call<ResponseBody> apiCall) {
		return stream(apiCall, false);
	}

    /**
     * Calls the Open AI api and returns a Flowable of SSE for streaming.
     * 
     * @param apiCall The api call
     * @param emitDone If true the last message ([DONE]) is emitted
     */
	public static Flowable<SSE> stream(Call<ResponseBody> apiCall, boolean emitDone) {
		return Flowable.create(emitter -> {
			apiCall.enqueue(new ResponseBodyCallback(emitter, emitDone));
		}, BackpressureStrategy.BUFFER);
	}

    /**
     * Calls the Open AI api and returns a Flowable of type T for streaming
     * omitting the last message.
     * 
     * @param apiCall The api call
     * @param cl Class of type T to return
     */
	public static <T> Flowable<T> stream(Call<ResponseBody> apiCall, Class<T> cl) {
		return stream(apiCall).map(sse -> {
			return errorMapper.readValue(sse.getData(), cl);
		});
	}

    /**
     * Shuts down the OkHttp ExecutorService.
     * The default behaviour of OkHttp's ExecutorService (ConnectionPool) 
     * is to shutdown after an idle timeout of 60s.
     * Call this method to shutdown the ExecutorService immediately.
     */
    public void shutdownExecutor(){
        this.executorService.shutdown();
    }

    public static OpenChatgptApi buildApi(OkHttpClient client) {
        ObjectMapper mapper = defaultObjectMapper();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        return retrofit.create(OpenChatgptApi.class);
    }

    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return mapper;
    }

    public static OkHttpClient defaultClient(String token, Duration timeout) {
        Objects.requireNonNull(token, "OpenAI token required");

        return new OkHttpClient.Builder()
                .addInterceptor(new AuthenticationInterceptor(token))
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    public static Retrofit defaultRetrofit(OkHttpClient client, ObjectMapper mapper) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}
