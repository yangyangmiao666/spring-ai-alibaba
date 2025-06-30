package com.alibaba.cloud.ai.example.graph.react.serializer;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateBuilder;
import com.alibaba.cloud.ai.graph.serializer.plain_text.PlainTextStateSerializer;
import com.alibaba.cloud.ai.graph.state.AgentStateFactory;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.content.Media;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverAllStateSerializer extends PlainTextStateSerializer {

    protected final ObjectMapper objectMapper;


    public OverAllStateSerializer(AgentStateFactory<OverAllState> stateFactory) {
        this(stateFactory, new ObjectMapper());
        this.objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    }

    public OverAllStateSerializer(AgentStateFactory<OverAllState> stateFactory, ObjectMapper objectMapper) {
        super(stateFactory);
        this.objectMapper = objectMapper;
    }

    @Override
    public String mimeType() {
        return "application/json";
    }

    @Override
    public void write(OverAllState object, ObjectOutput out) throws IOException {
        String json = objectMapper.writeValueAsString(object);
        out.writeUTF(json);
    }

    @Override
    public OverAllState read(ObjectInput in) throws IOException {
        String json = in.readUTF();
        OverAllState overAllState = objectMapper.readValue(json, OverAllState.class);
//        Map<String, Object> stateMap = objectMapper.readValue(json, Map.class);
        Map<String, Object> dataMap = overAllState.data();
        List<Object> messages = (List<Object>) dataMap.get("messages");
        List<Message> newMessages = new ArrayList<>();
        
        for (Object message : messages) {
            Map<String, Object> messageMap = (Map<String, Object>) message;
            if ("ASSISTANT".equals(messageMap.get("messageType"))) {
                List<Map<String, Object>> toolCallMapList = (List<Map<String, Object>>) messageMap.get("toolCalls");
                List<AssistantMessage.ToolCall> toolCalls = new ArrayList<>();
                for (Map<String, Object> toolCallMap : toolCallMapList) {
                    AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall(String.valueOf(toolCallMap.get("id")), String.valueOf(toolCallMap.get("type")), String.valueOf(toolCallMap.get("name")), String.valueOf(toolCallMap.get("arguments")));
                    toolCalls.add(toolCall);
                }
                AssistantMessage assistMess = new AssistantMessage(String.valueOf(messageMap.get("textContent")), (Map<String, Object>) messageMap.get("metadata"), toolCalls, (List<Media>) messageMap.get("media"));
                newMessages.add(assistMess);
            } else if ("USER".equals(messageMap.get("messageType"))) {
                UserMessage userMess = UserMessage.builder()
                        .text(String.valueOf(messageMap.get("textContent")))
                        .media((List<Media>) messageMap.get("media"))
                        .metadata((Map<String, Object>) messageMap.get("metadata")).build();
                newMessages.add(userMess);
            } else if ("SYSTEM".equals(messageMap.get("messageType"))) {
                SystemMessage systemMess = SystemMessage.builder()
                        .text(String.valueOf(messageMap.get("textContent")))
                        .metadata((Map<String, Object>) messageMap.get("metadata")).build();
                newMessages.add(systemMess);
            } else if ("TOOL".equals(messageMap.get("messageType"))) {
                List<Map<String, Object>> toolResponseMapList = (List<Map<String, Object>>) messageMap.get("responses");
                List<ToolResponseMessage.ToolResponse> newToolResponses = new ArrayList<>();
                for (Map<String, Object> toolResponseMap : toolResponseMapList) {
                    ToolResponseMessage.ToolResponse toolResponse = new ToolResponseMessage.ToolResponse(String.valueOf(toolResponseMap.get("id")), String.valueOf(toolResponseMap.get("name")), String.valueOf(toolResponseMap.get("responseData")));
                    newToolResponses.add(toolResponse);
                }
                ToolResponseMessage toolResponseMessage = new ToolResponseMessage(newToolResponses, (Map<String, Object>) messageMap.get("metadata"));
                newMessages.add(toolResponseMessage);
            }
        }
        Map<String, Object> newMap = new HashMap<>();
        newMap.put("messages", newMessages);
        OverAllState state = OverAllStateBuilder.builder()
                .withData(newMap)
                .withKeyStrategies(overAllState.keyStrategies())
                .setResume(overAllState.isResume())
                .build();
        return state;
    }
}
