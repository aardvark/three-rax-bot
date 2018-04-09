package ru.aardvark.sc.trax;

import SC2APIProtocol.Sc2Api;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SubProtocolHandler;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class Sc2ApiProtoHandler implements SubProtocolHandler {
  public static final Logger LOGGER = LoggerFactory.getLogger(Sc2ApiProtoHandler.class);

  @Override
  public List<String> getSupportedProtocols() {
    return Collections.singletonList("sc2Api");
  }

  @Override
  public void handleMessageFromClient(WebSocketSession session, WebSocketMessage<?> webSocketMessage,
                                      MessageChannel outputChannel) {
    Object payload = webSocketMessage.getPayload();
    Message<?> message = MessageBuilder.withPayload(payload).build();
    outputChannel.send(message);
  }

  @Override
  public void handleMessageToClient(WebSocketSession session, Message<?> message) throws Exception {
    LOGGER.debug("Output for session {}. Headers: {}", session, message.getHeaders());
    Object payload = message.getPayload();
    if (payload instanceof byte[]) {
      session.sendMessage(new BinaryMessage((byte[]) payload));
    }
  }

  @Override
  public String resolveSessionId(Message<?> message) {
    return null;
  }

  @Override
  public void afterSessionStarted(WebSocketSession session, MessageChannel outputChannel) {
    LOGGER.info("Sc2Api session started. Sending ping");
    try {
      handleMessageToClient(session, Sc2ApiMessageBuilder.ping());
    } catch (Exception e) {
      LOGGER.error("Error:", e);
    }
  }

  @Override
  public void afterSessionEnded(WebSocketSession session, CloseStatus closeStatus, MessageChannel outputChannel) {
    LOGGER.info("Sc2Api session ended with {}", closeStatus);
  }
}
