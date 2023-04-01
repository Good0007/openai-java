package com.theokanning.openai.conversation.chat;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ConversationContent implements Serializable {

    private String content_type = "text";
    private List<String> parts;

    public ConversationContent(List<String> parts) {
        this.content_type = "text";
        this.parts = parts;
    }

    public ConversationContent(String content_type, List<String> parts) {
        this.content_type = content_type;
        this.parts = parts;
    }

    public ConversationContent() {
    }
}

