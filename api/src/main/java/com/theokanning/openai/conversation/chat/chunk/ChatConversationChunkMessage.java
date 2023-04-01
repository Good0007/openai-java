package com.theokanning.openai.conversation.chat.chunk;
import com.theokanning.openai.conversation.chat.ConversationContent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Data
public class ChatConversationChunkMessage implements Serializable {

    private String id;
    Long create_time;
    String end_turn;
    Long update_time;
    String weight;
    String recipient;
    private ConversationContent content;
    private ChunkAuthor author;
    private Map<String,Object> metadata;
}