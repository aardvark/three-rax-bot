package ru.aardvark.sc.trax;

import SC2APIProtocol.Common;
import SC2APIProtocol.Sc2Api;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class Sc2ApiMessageBuilder {
  public static Message<byte[]> ping() {
    var requestBuilder = Sc2Api.Request.newBuilder();
    var ping = Sc2Api.RequestPing.getDefaultInstance();
    requestBuilder.setPing(ping);
    return toMessage(requestBuilder);
  }

  public static Message<byte[]> createGame() {
    var requestBuilder = Sc2Api.Request.newBuilder();
    var createGame = Sc2Api.RequestCreateGame.newBuilder();
    createGame.addPlayerSetup(
      Sc2Api.PlayerSetup.newBuilder().setType(Sc2Api.PlayerType.Computer).setRace(Common.Race.Zerg).build());
    createGame.addPlayerSetup(
      Sc2Api.PlayerSetup.newBuilder().setType(Sc2Api.PlayerType.Participant).setRace(Common.Race.Protoss).build());
    createGame.setBattlenetMapName("Acolyte LE");
    requestBuilder.setCreateGame(createGame);
    return toMessage(requestBuilder);
  }

  public static Message<byte[]> joinGame() {
    Sc2Api.Request.Builder requestBuilder = Sc2Api.Request.newBuilder();
    Sc2Api.RequestJoinGame.Builder requestJoinGame = Sc2Api.RequestJoinGame.newBuilder();
    requestJoinGame.setRace(Common.Race.Protoss);
    requestJoinGame.setOptions(Sc2Api.InterfaceOptions.newBuilder().setRaw(true));
    requestBuilder.setJoinGame(requestJoinGame);
    return toMessage(requestBuilder);
  }

  public static Message<byte[]> step(int x) {
    Sc2Api.Request.Builder requestBuilder = Sc2Api.Request.newBuilder();
    Sc2Api.RequestStep.Builder requestStep = Sc2Api.RequestStep.newBuilder();
    requestStep.setCount(x);
    requestBuilder.setStep(requestStep);
    return toMessage(requestBuilder);
  }

  public static Message<byte[]> observation() {
    var requestBuilder = Sc2Api.Request.newBuilder();
    var requestObservation = Sc2Api.RequestObservation.newBuilder();
    requestBuilder.setObservation(requestObservation);
    return toMessage(requestBuilder);
  }

  public static Message<byte[]> data() {
    var requestBuilder = Sc2Api.Request.newBuilder();
    var requestData = Sc2Api.RequestData.newBuilder();
    requestBuilder.setData(requestData);
    return toMessage(requestBuilder);
  }

  private static Message<byte[]> toMessage(Sc2Api.Request.Builder requestBuilder) {
    byte [] bytes = requestBuilder.build().toByteArray();
    return MessageBuilder.withPayload(bytes).build();
  }
}
