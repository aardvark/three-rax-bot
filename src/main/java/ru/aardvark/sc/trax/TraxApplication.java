package ru.aardvark.sc.trax;

import SC2APIProtocol.Sc2Api;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.websocket.ClientWebSocketContainer;
import org.springframework.integration.websocket.IntegrationWebSocketContainer;
import org.springframework.integration.websocket.inbound.WebSocketInboundChannelAdapter;
import org.springframework.integration.websocket.outbound.WebSocketOutboundMessageHandler;
import org.springframework.integration.websocket.support.SubProtocolHandlerRegistry;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.SubProtocolHandler;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.util.Collections;

@SpringBootApplication
public class TraxApplication {

  public static void main(String[] args) {
    SpringApplication.run(TraxApplication.class, args);
  }

  @Bean
  WebSocketClient webSocketClient() {
    WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
    webSocketContainer.setDefaultMaxBinaryMessageBufferSize(80_000);
    return new StandardWebSocketClient(webSocketContainer);
  }

  @Bean
  InstanceLauncher instanceLauncher() {
    return new InstanceLauncher("5000");
  }

  @Bean
  Agent agent(InstanceLauncher launcher) {
    Agent agent = new Agent(launcher);
    agent.start();
    return agent;
  }

  @Bean
  IntegrationWebSocketContainer clientContainer(Agent agent) {
    String webSocketClientUri = agent.getUri();
    return new ClientWebSocketContainer(webSocketClient(), webSocketClientUri);
  }

  @Bean
  StartGameDemo startGameDemo(MessageHandler outboundMessageHandler) {
    return new StartGameDemo(outboundMessageHandler, 5);
  }

  @Bean
  MessageChannel fromClientChannel(StartGameDemo demo) {
    DirectChannel channel = new DirectChannel();
    channel.setComponentName("sc2Api->Agent");
    channel.subscribe(demo);
    return channel;
  }

  @Bean
  SubProtocolHandler sc2ApiHandler() {
    return new Sc2ApiProtoHandler();
  }


  @Bean
  WebSocketInboundChannelAdapter inboundChannelAdapter(IntegrationWebSocketContainer clientContainer,
                                                       MessageChannel fromClientChannel) {
    WebSocketInboundChannelAdapter inboundChannelAdapter =
      new WebSocketInboundChannelAdapter(clientContainer, new SubProtocolHandlerRegistry(sc2ApiHandler()));
    inboundChannelAdapter.setOutputChannel(fromClientChannel);
    inboundChannelAdapter.setMergeWithDefaultConverters(false);
    inboundChannelAdapter.setMessageConverters(Collections.singletonList(new Sc2ApiMessageConverter()));
    inboundChannelAdapter.setPayloadType(Sc2Api.Response.class);
    return inboundChannelAdapter;
  }

  @Bean
  MessageHandler outboundMessageHandler(IntegrationWebSocketContainer clientContainer) {
    WebSocketOutboundMessageHandler outboundMessageHandler =
      new WebSocketOutboundMessageHandler(clientContainer, new SubProtocolHandlerRegistry(sc2ApiHandler()));
    outboundMessageHandler.setMessageConverters(Collections.singletonList(new Sc2ApiMessageConverter()));
    outboundMessageHandler.setComponentName("Agent->sc2Api");
    return outboundMessageHandler;
  }
}
