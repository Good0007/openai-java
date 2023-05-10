package com.theokanning.openai.completion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * Object containing a response chunk from the completions streaming api.
 *
 * https://beta.openai.com/docs/api-reference/completions/create
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompletionChunk {
    /**
     * A unique id assigned to this completion.
     */
    String id;

    /**https://beta.openai.com/docs/api-reference/create-completion
     * The type of object returned, should be "text_completion"
     */
    String object;

    /**
     * The creation time in epoch seconds.
     */
    long created;

    /**
     * The GPT-3 model used.
     */
    String model;

    /**
     * A list of generated completions.
     */
    List<CompletionChoice> choices;

    /**
     * 百度文心一言参数
     */
    String sentence_id;
    private Boolean is_end;
    private String result;
}
