package com.api.RestAPI.infrastructure.provider.legacylink.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "SendSmsResponse", namespace = "http://legacylink.fakecomworld.com/v1")
public class LegacyLinkResponse {

    @JacksonXmlProperty(localName = "StatusCode")
    private int statusCode;

    @JacksonXmlProperty(localName = "StatusMessage")
    private String statusMessage;

    @JacksonXmlProperty(localName = "MessageReference")
    private String messageReference;

    @JacksonXmlProperty(localName = "Timestamp")
    private String timestamp;

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getMessageReference() {
        return messageReference;
    }

    public String getTimestamp() {
        return timestamp;
    }
}