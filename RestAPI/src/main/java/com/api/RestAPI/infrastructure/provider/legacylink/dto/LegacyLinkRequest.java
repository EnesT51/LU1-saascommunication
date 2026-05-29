package com.api.RestAPI.infrastructure.provider.legacylink.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JacksonXmlRootElement(localName = "SendSmsRequest", namespace = "http://legacylink.fakecomworld.com/v1")
public class LegacyLinkRequest {

    @JacksonXmlProperty(localName = "PhoneNumber")
    private String phoneNumber;

    @JacksonXmlProperty(localName = "MessageText")
    private String messageText;

    @JacksonXmlProperty(localName = "SenderIdentification")
    private String senderIdentification;

    public LegacyLinkRequest() {
    }

    public LegacyLinkRequest(String phoneNumber, String messageText, String senderIdentification) {
        this.phoneNumber = phoneNumber;
        this.messageText = messageText;
        this.senderIdentification = senderIdentification;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSenderIdentification() {
        return senderIdentification;
    }
}