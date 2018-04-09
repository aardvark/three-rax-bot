package ru.aardvark.sc.trax;

import SC2APIProtocol.Sc2Api;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.ByteBuffer;

public class Sc2ApiMessageConverter implements MessageConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(Sc2ApiMessageConverter.class);
  public static final String MESSAGE_TYPE = "MessageType";

  @Override
  public Object fromMessage(Message<?> message, Class<?> targetClass) {
    Object payload = message.getPayload();
    if (payload instanceof ByteBuffer) {
      ByteBuffer payloadBuffer = (ByteBuffer) payload;
      Sc2Api.Response response = null;
      try {
        response = Sc2Api.Response.parseFrom(payloadBuffer.array());
        LOGGER.debug("Converted to {}:{}", response.getClass(), response);
      } catch (InvalidProtocolBufferException e) {
        LOGGER.error("Unable to convert response {}", e);
      }
      return response;
    }
    return null;
  }

  @Override
  public Message<?> toMessage(Object payload, MessageHeaders headers) {
    return MessageBuilder.createMessage(payload, headers);
  }
}
