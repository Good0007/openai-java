package com.theokanning.openai.conversation.chat.chunk;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChunkFinishDetails implements Serializable {
    private String type;
    private String stop;
}
