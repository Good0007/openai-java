package com.theokanning.openai.conversation.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
public class ChatConversationRequest implements Serializable {
    private String action = "next";
    //会话id
    private String conversation_id;
    private String model = "text-davinci-002-render-sha";
    //上一次消息
    private String parent_message_id;
    private Integer timezone_offset_min = -480;
    private List<ConversationMessage> messages;

    public ChatConversationRequest(){

    }

    public ChatConversationRequest(String conversation_id,ConversationMessage message){
        messages = new ArrayList<>();
        messages.add(message);
        this.conversation_id = conversation_id;
    }

    public ChatConversationRequest(String conversation_id,String parent_message_id,ConversationMessage message){
        messages = new ArrayList<>();
        messages.add(message);
        this.parent_message_id = parent_message_id;
    }


}
