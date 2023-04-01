package com.theokanning.openai.conversation.chat.chunk;
import lombok.Data;

import java.io.Serializable;


@Data
public class ChatConversationChunk implements Serializable {

    String error;
    private String conversation_id;
    private ChatConversationChunkMessage message;

}