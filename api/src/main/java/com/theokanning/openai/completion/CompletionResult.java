package com.theokanning.openai.completion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.theokanning.openai.Usage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * An object containing a response from the completion api
 *
 * https://beta.openai.com/docs/api-reference/completions/create
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompletionResult {
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
     * The API usage for this request
     */
    Usage usage;

    /**
     * 百度文心一言参数
     */
    String sentence_id;
    private Boolean is_end;
    private String result;
}
