/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.example.graph.react;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/react-human")
public class MyReactAgentWithHumanController {

    private final CompiledGraph myReactAgentGraph;

    MyReactAgentWithHumanController(@Qualifier("myReactAgentGraph") CompiledGraph myReactAgentGraph) {
        this.myReactAgentGraph = myReactAgentGraph;
    }

    @GetMapping("/chat")
    public String simpleChat(String query) {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId("1").build();
        Optional<OverAllState> result = myReactAgentGraph.invoke(Map.of("messages", new UserMessage(query)), runnableConfig);
        List<Message> messages = (List<Message>) result.get().value("messages").get();
        AssistantMessage assistantMessage = (AssistantMessage) messages.get(messages.size() - 1);
//		AssistantMessage assistantMessage = (AssistantMessage) result.get().value("messages").get();
        return assistantMessage.getText();
    }

    @GetMapping("/confirm")
    public String confirm() {
        String nextNode = "tool";
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId("1").build();

        StateSnapshot stateSnapshot = myReactAgentGraph.getState(runnableConfig);
        OverAllState state = stateSnapshot.state();

        System.out.println("=== Before confirm ===");
        System.out.println("State data: " + state.data());
        System.out.println("Resume: " + state.isResume());

        state.withResume();
        state.withHumanFeedback(new OverAllState.HumanFeedback(Map.of(), nextNode));

        System.out.println("=== After setting feedback ===");
        System.out.println("HumanFeedback: " + state.humanFeedback());
        System.out.println("Next node: " + state.humanFeedback().nextNodeId());

        Optional<OverAllState> result = myReactAgentGraph.invoke(state, runnableConfig);

        System.out.println("=== After invoke ===");
        System.out.println("Result data: " + (result.isPresent() ? result.get().data().toString() : "  "));

        return result.isPresent() ? result.get().data().toString() : "  ";
    }

    @GetMapping("/resume")
    public String resume() {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId("1").build();

        StateSnapshot stateSnapshot = myReactAgentGraph.getState(runnableConfig);
        OverAllState state = stateSnapshot.state();
        String nextNode = "tool";

        state.withResume();
        state.withHumanFeedback(new OverAllState.HumanFeedback(Map.of(), nextNode));

        Optional<OverAllState> result = myReactAgentGraph.invoke(state, runnableConfig);
        // send back to user and wait for plan approval

        return result.get().data().toString();
    }
}
