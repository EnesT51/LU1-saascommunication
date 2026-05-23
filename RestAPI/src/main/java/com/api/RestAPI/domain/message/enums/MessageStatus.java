package com.api.RestAPI.domain.message.enums;

public enum MessageStatus {
    QUEUED,
    PROCESSING,
    SENT,
    FAILED,
    RETRYING,
    DEAD_LETTER
}