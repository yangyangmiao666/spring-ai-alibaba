package com.alibaba.cloud.ai.example.graph.react;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class MyReactAgentWithHumanAutoconfiguration {

    @Bean
    public MyReactAgentWithHuman myReactAgent(ChatModel chatModel, ToolCallbackResolver resolver,ToolCallbackProvider toolCallbackProvider) throws GraphStateException {
        ChatClient chatClient = ChatClient.builder(chatModel)
//			.defaultToolNames("getWeatherFunction")
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(OpenAiChatOptions.builder().internalToolExecutionEnabled(false).build())
                .build();

        return MyReactAgentWithHuman.builder()
                .name("MyReactAgentWithHuman Demo")
                .chatClient(chatClient)
                .resolver(resolver)
                .maxIterations(10)
                .shouldContinueFunction((state) -> state.isResume() && state.humanFeedback() != null)
                .shouldInterruptFunction((state) -> !state.isResume() || state.humanFeedback() == null)
                .build();
    }

    @Bean
    public CompiledGraph myReactAgentGraph(@Qualifier("myReactAgent") MyReactAgentWithHuman reactAgent)
            throws GraphStateException {

        GraphRepresentation graphRepresentation = reactAgent.getStateGraph()
                .getGraph(GraphRepresentation.Type.PLANTUML);

        System.out.println("\n\n");
        System.out.println(graphRepresentation.content());
        System.out.println("\n\n");

        return reactAgent.getAndCompileGraph();
    }

//    @Bean
//    public RestClient.Builder createRestClient() {
//
//        // 2. 创建 RequestConfig 并设置超时
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectTimeout(Timeout.of(10, TimeUnit.MINUTES)) // 设置连接超时
//                .setResponseTimeout(Timeout.of(10, TimeUnit.MINUTES))
//                .setConnectionRequestTimeout(Timeout.of(10, TimeUnit.MINUTES))
//                .build();
//
//        // 3. 创建 CloseableHttpClient 并应用配置
//        HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
//
//        // 4. 使用 HttpComponentsClientHttpRequestFactory 包装 HttpClient
//        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
//
//        // 5. 创建 RestClient 并设置请求工厂
//        return RestClient.builder().requestFactory(requestFactory);
//    }

}
