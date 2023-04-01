package com.theokanning.openai;

import com.theokanning.openai.conversation.chat.ChatConversationRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface OpenChatgptApi {

    @Streaming
    @POST("/backend-api/conversation")
    Call<ResponseBody> createChatConversationStream(@Body ChatConversationRequest request);

}
