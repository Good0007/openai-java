package com.theokanning.openai.conversation.chat.chunk;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ChunkAuthor implements Serializable {
    private String role;
    private String name;
    private Map<String,Object> metadata;
}
