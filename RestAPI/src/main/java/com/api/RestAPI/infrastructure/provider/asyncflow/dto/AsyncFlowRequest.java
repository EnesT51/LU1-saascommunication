package com.api.RestAPI.infrastructure.provider.asyncflow.dto;

public class AsyncFlowRequest {

    private String destination;
    private String content;
    private String priority;

    public AsyncFlowRequest(String destination, String content, String priority) {
        this.destination = destination;
        this.content = content;
        this.priority = priority;
    }

    public String getDestination() {
        return destination;
    }

    public String getContent() {
        return content;
    }

    public String getPriority() {
        return priority;
    }
}