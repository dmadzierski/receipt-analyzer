package pl.madzierski.daniel;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.event.ConsistencyTokens;
import pl.madzierski.daniel.readmodel.ReadModelEventListener;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Import(HttpApiIT.TestJwtDecoder.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReadModelPipelineIT extends IntegrationTest {

    private static final String CONTEXT_PATH = "/api";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KafkaListenerEndpointRegistry listenerRegistry;

    private MessageListenerContainer listener;
    private String userSub;

    @BeforeEach
    void startPipeline() throws Exception {
        listener = listenerRegistry.getListenerContainer(ReadModelEventListener.LISTENER_ID);
        listener.start();
        await().atMost(Duration.ofSeconds(30)).until(() -> !listener.getAssignedPartitions().isEmpty());
        userSub = "pipeline-user-" + UUID.randomUUID();
        mockMvc.perform(as(post(CONTEXT_PATH + "/users/sync/me"))).andExpect(status().isOk());
    }

    @AfterEach
    void stopPipelineAndRemoveData() {
        listener.stop();
        jdbcTemplate.update("DELETE FROM wallet WHERE user_id IN (SELECT id FROM app_user WHERE user_sub = ?)", userSub);
        jdbcTemplate.update("DELETE FROM app_user WHERE user_sub = ?", userSub);
        jdbcTemplate.update("DELETE FROM event_publication");
    }

    @Test
    void readAfterCommandWithItsTokenSeesTheChangeThroughKafkaAndMongo() throws Exception {
        MockHttpServletResponse created = mockMvc.perform(as(post(CONTEXT_PATH + "/wallets"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Home\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse();
        String token = created.getHeader(ConsistencyTokens.HEADER);
        String walletId = JsonPath.read(created.getContentAsString(), "$.id");

        MockHttpServletResponse wallets = mockMvc.perform(as(get(CONTEXT_PATH + "/wallets")).header(ConsistencyTokens.HEADER, token))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        assertThat(token).matches("[0-9a-f-]{36}:1");
        assertThat(wallets.getHeader("X-Read-Model-Stale")).isNull();
        assertThat((String) JsonPath.read(wallets.getContentAsString(), "$.items[0].id")).isEqualTo(walletId);
    }

    @Test
    void readWithTokenThatNeverCatchesUpIsMarkedAsStale() throws Exception {
        MockHttpServletResponse wallets = mockMvc.perform(as(get(CONTEXT_PATH + "/wallets"))
                .header(ConsistencyTokens.HEADER, UUID.randomUUID() + ":1"))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        assertThat(wallets.getHeader("X-Read-Model-Stale")).isEqualTo("true");
    }

    @Test
    void commandThatChangesNothingReturnsNoToken() throws Exception {
        MockHttpServletResponse synced = mockMvc.perform(as(post(CONTEXT_PATH + "/users/sync/me")))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        assertThat(synced.getHeader(ConsistencyTokens.HEADER)).isNull();
    }

    private MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request) {
        return request.contextPath(CONTEXT_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + userSub);
    }
}
