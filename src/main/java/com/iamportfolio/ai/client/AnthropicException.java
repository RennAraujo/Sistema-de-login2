package com.iamportfolio.ai.client;

/** Wraps any failure talking to the Anthropic Messages API. */
public class AnthropicException extends RuntimeException {
    public AnthropicException(String message, Throwable cause) {
        super(message, cause);
    }
}
