package com.hemovia.api.controller;

import com.hemovia.api.controller.handler.SecurityErrorHandler;
import com.hemovia.api.controller.security.SecurityConfig;
import com.hemovia.api.controller.security.SessionCookies;
import com.hemovia.api.dto.request.DonationChatRequest.Message;
import com.hemovia.api.model.User;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.exception.DonationChatUnavailableException;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.service.config.DonationChatConfiguration;
import com.hemovia.api.service.config.HemoviaProperties;
import com.hemovia.api.service.config.SystemClockProvider;
import com.hemovia.api.service.port.output.ClockProvider;
import com.hemovia.api.service.port.output.DonationChatInterpreter;
import com.hemovia.api.service.security.JwtAccessTokenIssuer;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DonationChatController.class, properties = {
        "logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor=TRACE",
        "logging.level.org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver=DEBUG"})
@Import({SecurityConfig.class, SecurityErrorHandler.class, DonationChatConfiguration.class, JwtAccessTokenIssuer.class,
        DonationChatIntegrationTest.SessionConfiguration.class})
@ExtendWith(OutputCaptureExtension.class)
class DonationChatIntegrationTest {

    private static final String PATH = "/api/v1/donation-chat/messages";
    private static final String TATTOO_SOURCE =
            "https://bvsms.saude.gov.br/bvs/saudelegis/gm/2026/prt11685_03_07_2026.html";
    private static final String QUESTION = """
            {"messages":[{"role":"user","content":"Fiz uma tatuagem há três meses. Posso doar?"}]}
            """;
    private static final JsonMapper JSON = new JsonMapper();

    @Autowired private MockMvc mvc;
    @Autowired private JwtAccessTokenIssuer tokens;
    @Autowired private TestInterpreter interpreter;

    @BeforeEach
    void resetInterpreter() {
        interpreter.calls.clear();
        interpreter.replies.clear();
        interpreter.unavailable = false;
    }

    @Test
    @DisplayName("@spec:AC-132 papel de doador sem vínculo, autenticação, CSRF e fontes oficiais")
    void requiresDonorSessionAndCsrf() throws Exception {
        Cookie donor = session(Role.DOADOR);
        interpreter.replies.put(List.of(new Message("user", "Fiz uma tatuagem há três meses. Posso doar?")),
                "tatuagem-tres-meses");

        mvc.perform(withCsrf(request(QUESTION))).andExpect(status().isUnauthorized());
        mvc.perform(withCsrf(request(QUESTION).cookie(session(Role.ADMIN)))).andExpect(status().isForbidden());
        mvc.perform(request(QUESTION).cookie(donor)).andExpect(status().isForbidden());
        assertThat(interpreter.calls).isEmpty();

        ask(donor, QUESTION).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.answer").value(containsString("três meses")))
                .andExpect(jsonPath("$.sources[0].title").value("Portaria GM/MS nº 11.685/2026"))
                .andExpect(jsonPath("$.sources[0].url").value(TATTOO_SOURCE));
    }

    @Test
    @DisplayName("@spec:AC-133 conversa usa o histórico enviado, em ordem, e uma pergunta isolada pede esclarecimento")
    void followUpUsesOnlySubmittedConversation() throws Exception {
        Message initial = new Message("user", "Fiz uma tatuagem.");
        Message previousAnswer = new Message("assistant", "Quando você fez a tatuagem?");
        Message followUp = new Message("user", "Há três meses. E agora?");
        List<Message> history = List.of(initial, previousAnswer, followUp);
        interpreter.replies.put(history, "tatuagem-tres-meses");
        interpreter.replies.put(List.of(followUp), "esclarecimento");
        Cookie donor = session(Role.DOADOR);

        ask(donor, body(history)).andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(containsString("a de quatro meses ainda não foi cumprida")));
        ask(donor, body(List.of(followUp))).andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(containsString("Você pode esclarecer")));
        assertThat(interpreter.calls).containsExactly(history, List.of(followUp));
    }

    @Test
    @DisplayName("@spec:AC-138 limites e papéis são validados antes da inferência")
    void invalidInputNeverReachesInference() throws Exception {
        Cookie donor = session(Role.DOADOR);
        Message question = new Message("user", "Dúvida");
        List<String> invalid = List.of(
                "{}", "{\"messages\":null}", "{\"messages\":[null]}",
                "{\"messages\":[{\"role\":\"user\",\"content\":123}]}",
                "{\"messages\":[{\"role\":\"user\",\"content\":true}]}",
                body(List.of()), body(Collections.nCopies(10, question)),
                body(List.of(new Message("user", "a".repeat(1001)))),
                body(List.of(new Message("user", " \t\n "))),
                body(List.of(new Message("system", "Dúvida"))),
                body(List.of(question, new Message("assistant", "Uma resposta"))));

        for (String request : invalid) {
            ask(donor, request).andExpect(status().isBadRequest());
        }
        assertThat(interpreter.calls).isEmpty();

        List<Message> maximum = Collections.nCopies(9, new Message("user", "a".repeat(1000)));
        interpreter.replies.put(maximum, "encaminhamento");
        ask(donor, body(maximum)).andExpect(status().isOk());
        assertThat(interpreter.calls).containsExactly(maximum);
    }

    @Test
    @DisplayName("@spec:AC-138 inferência recebe somente a mensagem e o conteúdo fica fora dos logs do MVC")
    void onlyConversationIsForwardedAndNotLogged(CapturedOutput output) throws Exception {
        Message message = new Message("user", "MARCADOR-SINTÉTICO-DE-PRIVACIDADE");
        interpreter.replies.put(List.of(message), "requisitos");

        Cookie donor = session(Role.DOADOR);
        ask(donor, body(List.of(new Message("user", "  " + message.content() + "  "))))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(message.content()))));
        String invalidMarker = "MARCADOR-DADO-RECUSADO";
        ask(donor, body(List.of(new Message("user", invalidMarker + "a".repeat(1001)))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString(invalidMarker))));

        String malformedMarker = "MARCADORJSONMALFORMADO";
        String malformed = "{\"messages\":[{\"role\":\"user\",\"content\":" + malformedMarker + "}]}";
        ask(donor, malformed).andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString(malformedMarker))));

        assertThat(interpreter.calls).containsExactly(List.of(message));
        assertThat(output.getAll()).contains("DonationChatRequest", "Resolved [")
                .doesNotContain(message.content(), invalidMarker, malformedMarker);
    }

    @Test
    @DisplayName("@spec:AC-134 falha da inferência produz HTTP 503 com mensagem pública")
    void inferenceFailureReturnsServiceUnavailable() throws Exception {
        interpreter.unavailable = true;

        ask(session(Role.DOADOR), QUESTION).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("DONATION_CHAT_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value(containsString("temporariamente indisponível")));
    }

    private ResultActions ask(Cookie session, String body) throws Exception {
        return mvc.perform(withCsrf(request(body).cookie(session)));
    }

    private static MockHttpServletRequestBuilder request(String body) {
        return post(PATH).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) {
        String token = UUID.randomUUID().toString();
        return request.cookie(new Cookie("XSRF-TOKEN", token)).header("X-XSRF-TOKEN", token);
    }

    private static String body(List<Message> messages) {
        return JSON.writeValueAsString(Map.of("messages", messages));
    }

    private Cookie session(Role role) {
        User person = User.restore(UUID.randomUUID(), "Pessoa de teste", new Email("doador@example.test"),
                new Cpf("52998224725"), null, "hash", role, true, null, null, null);
        return new Cookie(SessionCookies.ACCESS, tokens.issue(person).value());
    }

    static final class TestInterpreter implements DonationChatInterpreter {

        final List<List<Message>> calls = new ArrayList<>();
        final Map<List<Message>, String> replies = new HashMap<>();
        boolean unavailable;

        @Override
        public List<String> selectCards(List<Message> messages) {
            calls.add(messages);
            if (unavailable) {
                throw new DonationChatUnavailableException();
            }
            return List.of(replies.getOrDefault(messages, "encaminhamento"));
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SessionConfiguration {

        @Bean
        @Primary
        TestInterpreter testInterpreter() {
            return new TestInterpreter();
        }

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }

        @Bean
        ClockProvider clockProvider(Clock clock) {
            return new SystemClockProvider(clock);
        }

        @Bean
        HemoviaProperties hemoviaProperties() {
            return new HemoviaProperties(new HemoviaProperties.Security(
                    "segredo-de-teste-para-o-chat-com-mais-de-32-caracteres", 15, 7, false, "Lax",
                    "http://localhost:5173"), new HemoviaProperties.AdminBootstrap(null, null, null, null));
        }
    }
}

