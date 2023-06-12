package com.theokanning.openai.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.OpenAiError;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.CompletionChunk;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.dashboard.credit.DashboardCreditSummary;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.edit.EditResult;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.embedding.EmbeddingSingleRequest;
import com.theokanning.openai.file.File;
import com.theokanning.openai.finetune.FineTuneEvent;
import com.theokanning.openai.finetune.FineTuneRequest;
import com.theokanning.openai.finetune.FineTuneResult;
import com.theokanning.openai.image.CreateImageEditRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.CreateImageVariationRequest;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.model.Model;
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.moderation.ModerationResult;

import com.theokanning.openai.service.config.ServiceConfigProperties;
import com.theokanning.openai.service.interceptor.AuthenticationInterceptor;
import com.theokanning.openai.service.interceptor.ConnectTimoutRetryInterceptor;
import com.theokanning.openai.service.interceptor.QueryParamsInterceptor;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.*;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class OpenAiService {

    private ServiceConfigProperties configProperties;
    private String apiUrl = "https://api.openai.com/";
    private String token = "";
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(100);
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;

    private final ObjectMapper errorMapper = defaultObjectMapper();
    private OkHttpClient httpClient;
    private OpenAiApi api;
    private ExecutorService executorService;

    public String getApiUrl() {
        return apiUrl;
    }

    public ObjectMapper getErrorMapper() {
        return errorMapper;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public OpenAiApi getApi() {
        return api;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     */
    public OpenAiService(final String token) {
        this(token, DEFAULT_READ_TIMEOUT);
    }

    public OpenAiService(final ServiceConfigProperties configProperties) {
        this.configProperties = configProperties;
        this.token = configProperties.getTokenKey();
        this.apiUrl = configProperties.getApiUrl();
        this.httpClient = this.defaultClient(configProperties.getTokenKey(),DEFAULT_READ_TIMEOUT);
        this.api = this.buildApi(httpClient);
        settingExecutor();
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token   OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     * @param timeout http read timeout, Duration.ZERO means no timeout
     */
    public OpenAiService(final String token, final Duration timeout) {
        this.token = token;
        this.httpClient = this.defaultClient(token,timeout);
        this.api = this.buildApi(httpClient);
        settingExecutor();
    }

    /**
     *
     * @param token
     * @param apiUrl
     * @param timeout
     */
    public OpenAiService(final String token, final String apiUrl, final Duration timeout) {
        this.token = token;
        this.apiUrl = apiUrl;
        this.httpClient = this.defaultClient(token,timeout);
        this.api = this.buildApi(httpClient);
        settingExecutor();
    }

    /**
     * build from client
     * @param client
     */
    public OpenAiService(final OkHttpClient client) {
        this.httpClient = client;
        this.api = this.buildApi(httpClient);
        settingExecutor();
    }

    private void settingExecutor(){
        httpClient.dispatcher().setMaxRequests(200);
        httpClient.dispatcher().setMaxRequestsPerHost(50);
        this.executorService = httpClient.dispatcher().executorService();
    }

    public List<Model> listModels() {
        return execute(api.listModels()).data;
    }

    public Model getModel(String modelId) {
        return execute(api.getModel(modelId));
    }

    public CompletionResult createCompletion(CompletionRequest request) {
        return execute(api.createCompletion(request));
    }

    public Flowable<byte[]> streamCompletionBytes(CompletionRequest request) {
		request.setStream(true);
		return stream(api.createCompletionStream(request), true).map(sse -> {
			return sse.toBytes();
		});
	}
    
    public Flowable<CompletionChunk> streamCompletion(CompletionRequest request) {
		request.setStream(true);
        
		return stream(api.createCompletionStream(request), CompletionChunk.class);
	}
    
    public ChatCompletionResult createChatCompletion(ChatCompletionRequest request) {
        return execute(api.createChatCompletion(request));
    }

    public Flowable<byte[]> streamChatCompletionBytes(ChatCompletionRequest request) {
		request.setStream(true);

		return stream(api.createChatCompletionStream(request), true).map(sse -> {
			return sse.toBytes();
		});
	}

	public Flowable<ChatCompletionChunk> streamChatCompletion(ChatCompletionRequest request) {
		request.setStream(true);
        
		return stream(api.createChatCompletionStream(request), ChatCompletionChunk.class);
	}

    public EditResult createEdit(EditRequest request) {
        return execute(api.createEdit(request));
    }


    public DashboardCreditSummary getDashboardCreditGrants() {
        return execute(api.getDashboardCreditGrants());
    }

    public EmbeddingResult createEmbeddings(EmbeddingRequest request) {
        return execute(api.createEmbeddings(request));
    }

    public EmbeddingResult createEmbeddings(EmbeddingSingleRequest request) {
        return execute(api.createEmbeddings(request));
    }

    public List<File> listFiles() {
        return execute(api.listFiles()).data;
    }

    public File uploadFile(String purpose, String filepath) {
        java.io.File file = new java.io.File(filepath);
        RequestBody purposeBody = RequestBody.create(okhttp3.MultipartBody.FORM, purpose);
        RequestBody fileBody = RequestBody.create(MediaType.parse("text"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", filepath, fileBody);

        return execute(api.uploadFile(purposeBody, body));
    }

    public DeleteResult deleteFile(String fileId) {
        return execute(api.deleteFile(fileId));
    }

    public File retrieveFile(String fileId) {
        return execute(api.retrieveFile(fileId));
    }

    public FineTuneResult createFineTune(FineTuneRequest request) {
        return execute(api.createFineTune(request));
    }

    public CompletionResult createFineTuneCompletion(CompletionRequest request) {
        return execute(api.createFineTuneCompletion(request));
    }

    public List<FineTuneResult> listFineTunes() {
        return execute(api.listFineTunes()).data;
    }

    public FineTuneResult retrieveFineTune(String fineTuneId) {
        return execute(api.retrieveFineTune(fineTuneId));
    }

    public FineTuneResult cancelFineTune(String fineTuneId) {
        return execute(api.cancelFineTune(fineTuneId));
    }

    public List<FineTuneEvent> listFineTuneEvents(String fineTuneId) {
        return execute(api.listFineTuneEvents(fineTuneId)).data;
    }

    public DeleteResult deleteFineTune(String fineTuneId) {
        return execute(api.deleteFineTune(fineTuneId));
    }

    public ImageResult createImage(CreateImageRequest request) {
        return execute(api.createImage(request));
    }

    public ImageResult createImageEdit(CreateImageEditRequest request, String imagePath, String maskPath) {
        java.io.File image = new java.io.File(imagePath);
        java.io.File mask = null;
        if (maskPath != null) {
            mask = new java.io.File(maskPath);
        }
        return createImageEdit(request, image, mask);
    }

    public ImageResult createImageEdit(CreateImageEditRequest request, java.io.File image, java.io.File mask) {
        RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("prompt", request.getPrompt())
                .addFormDataPart("size", request.getSize())
                .addFormDataPart("response_format", request.getResponseFormat())
                .addFormDataPart("image", "image", imageBody);

        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }

        if (mask != null) {
            RequestBody maskBody = RequestBody.create(MediaType.parse("image"), mask);
            builder.addFormDataPart("mask", "mask", maskBody);
        }

        return execute(api.createImageEdit(builder.build()));
    }

    public ImageResult createImageVariation(CreateImageVariationRequest request, String imagePath) {
        java.io.File image = new java.io.File(imagePath);
        return createImageVariation(request, image);
    }

    public ImageResult createImageVariation(CreateImageVariationRequest request, java.io.File image) {
        RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("size", request.getSize())
                .addFormDataPart("response_format", request.getResponseFormat())
                .addFormDataPart("image", "image", imageBody);

        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }

        return execute(api.createImageVariation(builder.build()));
    }

    public ModerationResult createModeration(ModerationRequest request) {
        return execute(api.createModeration(request));
    }


    /*** 文心一言 ****/
    public ChatCompletionResult createBaiduChatCompletion(ChatCompletionRequest request) {
        return execute(api.createBaiduChatCompletion(request));
    }


    public Flowable<ChatCompletionChunk> streamBaiduChatCompletion(ChatCompletionRequest request) {
        request.setStream(true);
        return stream(api.createBaiduChatCompletionStream(request), ChatCompletionChunk.class);
    }
    /*** 文心一言 ****/



    /**
     * Calls the Open AI api, returns the response, and parses error messages if the request fails
     */
    public <T> T execute(Single<T> apiCall) {
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
	public <T> Flowable<T> stream(Call<ResponseBody> apiCall, Class<T> cl) {
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

    public OpenAiApi buildApi(OkHttpClient client) {
        ObjectMapper mapper = defaultObjectMapper();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        return retrofit.create(OpenAiApi.class);
    }

    public ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return mapper;
    }

    public OkHttpClient defaultClient(String token, Duration timeout) {
        Objects.requireNonNull(token, "OpenAI token required");
        Interceptor authInterceptor;
        if(apiUrl.contains("aip.baidubce.com")){
            //文心一言token加入
            Map<String,String> param = new HashMap<>();
            param.put("access_token",token);
            authInterceptor = new QueryParamsInterceptor(param);
        } else {
            //其他类别的认证方式
            authInterceptor = new AuthenticationInterceptor(token);
        }
        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectionPool(new ConnectionPool(64, 10, TimeUnit.SECONDS))
                .readTimeout(DEFAULT_READ_TIMEOUT.getSeconds(),TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                //超时重试
                .addInterceptor(new ConnectTimoutRetryInterceptor(2))
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT,TimeUnit.SECONDS)
                .build();

    }

    public Retrofit defaultRetrofit(OkHttpClient client, ObjectMapper mapper) {
        return new Retrofit.Builder()
                .baseUrl(apiUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public String getToken() {
        return token;
    }

    public ServiceConfigProperties getConfigProperties() {
        return configProperties;
    }

    public void setConfigProperties(ServiceConfigProperties configProperties) {
        this.configProperties = configProperties;
    }
}
