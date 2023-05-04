package com.theokanning.openai.service.config;

import java.io.Serializable;

public class ServiceConfigProperties implements Serializable {
    private String apiUrl = "https://api.openai.com/";
    private String tokenId;
    private String tokenName;
    private String tokenKey;
    private String tag;
    private String robotType = "chatgpt";
    private String robotModel = "gpt-3.5-turbo";
    private Integer maxTokens = 3800;
    private Integer requestMaxTokens = 2000;
    private String envType;

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = tokenKey;
    }

    public String getRobotType() {
        return robotType;
    }

    public void setRobotType(String robotType) {
        this.robotType = robotType;
    }

    public String getRobotModel() {
        return robotModel;
    }

    public void setRobotModel(String robotModel) {
        this.robotModel = robotModel;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Integer getRequestMaxTokens() {
        return requestMaxTokens;
    }

    public void setRequestMaxTokens(Integer requestMaxTokens) {
        this.requestMaxTokens = requestMaxTokens;
    }

    public String getEnvType() {
        return envType;
    }

    public void setEnvType(String envType) {
        this.envType = envType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "ServiceConfigProperties{" +
                "apiUrl='" + apiUrl + '\'' +
                ", tokenId='" + tokenId + '\'' +
                ", tokenName='" + tokenName + '\'' +
                ", tokenKey='" + tokenKey + '\'' +
                ", tag='" + tag + '\'' +
                ", robotType='" + robotType + '\'' +
                ", robotModel='" + robotModel + '\'' +
                ", maxTokens=" + maxTokens +
                ", requestMaxTokens=" + requestMaxTokens +
                ", envType='" + envType + '\'' +
                '}';
    }
}
