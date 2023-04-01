package com.theokanning.openai.conversation.chat;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;

@Data
public class ConversationMessage implements Serializable {
    private String id;
    private HashMap<String,String> author;
    private ConversationContent content;

    public ConversationMessage(String text) {
        this.content = new ConversationContent(Collections.singletonList(text));
        author = new HashMap<>();
        author.put("role","user");
    }

    public ConversationMessage() {
    }
}
