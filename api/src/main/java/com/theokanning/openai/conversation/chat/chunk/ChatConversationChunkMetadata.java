package com.theokanning.openai.conversation.chat.chunk;

import lombok.Data;

import java.io.Serializable;


@Data
public class ChatConversationChunkMetadata implements Serializable {
    /**
     * "message_type": "next",
     * 	"model_slug": "text-davinci-002-render-sha",
     * 	"finish_details": {
     * 	    "type": "stop",
     * 	    "stop": "<|im_end|>"
     *    }
     */
    private String message_type;
    private String model_slug;
    private ChunkFinishDetails finish_details;

}