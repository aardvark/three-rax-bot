package ru.aardvark.sc.trax;

import SC2APIProtocol.Sc2Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * Repeatedly loop game with step increments.
 * Restart game when it ended.
 * Observation requested every incremental step.
 */
public class StartGameDemo implements MessageHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(StartGameDemo.class);
  private final MessageHandler toClient;
  private final int stepIncrement;

  public StartGameDemo(MessageHandler outboundMessageHandler, int stepIncrement) {
    this.toClient = outboundMessageHandler;
    this.stepIncrement = stepIncrement;
  }

  public void handleMessageInternal(Sc2Api.Response response) throws MessagingException {
    var status = response.getStatus();
    var responseCase = response.getResponseCase();
    LOGGER.info("Status: {}, messageType: {}", status, responseCase);
    convert(response, responseCase, status);
  }

  private void convert(Sc2Api.Response response, Sc2Api.Response.ResponseCase responseCase, Sc2Api.Status status) {
    switch (responseCase) {
      case PING:
        process(response.getPing());
        break;
      case CREATE_GAME:
        process(response.getCreateGame());
        break;
      case JOIN_GAME:
        process(response.getJoinGame());
        break;
      case STEP:
        process(response.getStep(), status);
        break;
      case OBSERVATION:
        process(response.getObservation());
        break;
      case RESPONSE_NOT_SET:
        process(response.getStatus());
        break;
      default:
        LOGGER.info("{} -- handler undefined", responseCase);
        break;
    }
  }

  private void process(Sc2Api.Status status) {
    createIfEnded(status);

  }

  private void createIfEnded(Sc2Api.Status status) {
    switch (status) {
      case in_game:
        toClient.handleMessage(Sc2ApiMessageBuilder.observation());
        break;
      case ended:
        toClient.handleMessage(Sc2ApiMessageBuilder.createGame());
        break;
    }
  }

  private void process(Sc2Api.ResponseObservation observation) {
    toClient.handleMessage(Sc2ApiMessageBuilder.step(stepIncrement));
  }

  private void process(Sc2Api.ResponseStep step, Sc2Api.Status status) {
    createIfEnded(status);
  }

  private void process(Sc2Api.ResponseJoinGame joinGame) {
    toClient.handleMessage(Sc2ApiMessageBuilder.step(1));
  }

  private void process(Sc2Api.ResponseCreateGame createGame) {
    toClient.handleMessage(Sc2ApiMessageBuilder.joinGame());
  }

  private void process(Sc2Api.ResponsePing ping) {
    LOGGER.info("{}", ping);
    toClient.handleMessage(Sc2ApiMessageBuilder.createGame());
  }

  @Override
  public void handleMessage(Message<?> message) throws MessagingException {
    handleMessageInternal((Sc2Api.Response) message.getPayload());
  }
}
