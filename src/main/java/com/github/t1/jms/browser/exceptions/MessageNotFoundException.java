package com.github.t1.jms.browser.exceptions;

public class MessageNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String messageId;
    private final String queueName;

    public MessageNotFoundException(String messageId, String queueName) {
        this.messageId = messageId;
        this.queueName = queueName;
    }

    @Override
    public String getMessage() {
        return "there's no message " + messageId + " in queue " + queueName;
    }
}
