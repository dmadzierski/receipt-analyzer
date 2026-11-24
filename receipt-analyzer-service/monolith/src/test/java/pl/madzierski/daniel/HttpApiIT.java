package pl.madzierski.daniel;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Import(TestJwtDecoder.class)
class HttpApiIT extends IntegrationTest {

    private static final String CONTEXT_PATH = "/api";

    @Autowired
    private MockMvc mockMvc;

    private String userSub;

    @BeforeEach
    void registerUser() throws Exception {
        userSub = "http-user-" + UUID.randomUUID();
        mockMvc.perform(as(userSub, post(CONTEXT_PATH + "/users/sync/me"))).andExpect(status().isOk());
        flush();
    }

    @Test
    void requestsWithoutValidTokenAreUnauthorized() throws Exception {
        mockMvc.perform(api(get(CONTEXT_PATH + "/wallets"))).andExpect(status().isUnauthorized());
        mockMvc.perform(as(TestJwtDecoder.INVALID_TOKEN, get(CONTEXT_PATH + "/wallets"))).andExpect(status().isUnauthorized());
    }

    @Test
    void healthIsAvailableWithoutToken() throws Exception {
        mockMvc.perform(api(get(CONTEXT_PATH + "/actuator/health"))).andExpect(status().isOk());
    }

    @Test
    void tokenOfUserThatWasNeverSynchronizedIsRejected() throws Exception {
        expectError(mockMvc.perform(as("never-synced-" + UUID.randomUUID(), get(CONTEXT_PATH + "/wallets"))),
            400, "EXCEPTION.USER_NOT_FOUND");
    }

    @Test
    void synchronizingSameUserTwiceKeepsOneAccount() throws Exception {
        mockMvc.perform(as(userSub, post(CONTEXT_PATH + "/users/sync/me"))).andExpect(status().isOk());

        assertThat(count("SELECT count(*) FROM app_user WHERE user_sub = ?", userSub)).isEqualTo(1);
    }

    @Test
    void createdWalletReturnsOnlyIdAndIsVisibleOnlyToItsOwner() throws Exception {
        String walletId = idOf(mockMvc.perform(json(as(userSub, post(CONTEXT_PATH + "/wallets")), "{\"name\": \"  Home \"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*", hasSize(1))));
        flush();

        mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/wallets")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].id").value(walletId))
            .andExpect(jsonPath("$.items[0].name").value("Home"));
        mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/wallets/" + walletId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Home"))
            .andExpect(jsonPath("$.receipts").doesNotExist());
        mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/receipts").param("walletId", walletId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receipts", hasSize(0)));

        String stranger = registerOtherUser();
        mockMvc.perform(as(stranger, get(CONTEXT_PATH + "/wallets")))
            .andExpect(jsonPath("$.items", hasSize(0)));
        expectError(mockMvc.perform(as(stranger, get(CONTEXT_PATH + "/wallets/" + walletId))), 400, "EXCEPTION.WALLET_NOT_FOUND");
        expectError(mockMvc.perform(as(stranger, get(CONTEXT_PATH + "/receipts").param("walletId", walletId))), 400, "EXCEPTION.WALLET_NOT_FOUND");
    }

    @Test
    void invalidRequestsAreRejectedWithInvalidRequestCode() throws Exception {
        expectError(mockMvc.perform(json(as(userSub, post(CONTEXT_PATH + "/wallets")), "{\"name\": \"   \"}")),
            400, "EXCEPTION.INVALID_REQUEST");
        expectError(mockMvc.perform(json(as(userSub, post(CONTEXT_PATH + "/wallets")), "{not json")),
            400, "EXCEPTION.INVALID_REQUEST");
        expectError(mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/wallets/not-a-uuid"))),
            400, "EXCEPTION.INVALID_REQUEST");
        expectError(mockMvc.perform(json(as(userSub, post(CONTEXT_PATH + "/stores")),
                "{\"brandName\": \" \", \"address\": \"ul. Testowa 1\", \"city\": \"Poznań\"}")),
            400, "EXCEPTION.INVALID_REQUEST");
        assertThat(count("SELECT count(*) FROM wallet")).isZero();
        assertThat(count("SELECT count(*) FROM store")).isZero();
    }

    @Test
    void storeIsCreatedWithNewBrandAndThenReadByItsId() throws Exception {
        String storeId = idOf(mockMvc.perform(json(as(userSub, post(CONTEXT_PATH + "/stores")),
                "{\"brandName\": \" Lidl \", \"address\": \"ul. Testowa 1\", \"city\": \"Poznań\", \"postalCode\": \"60-001\", \"country\": \"Polska\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.*", hasSize(1))));
        flush();

        mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/stores/" + storeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.brand").value("Lidl"))
            .andExpect(jsonPath("$.city").value("Poznań"));
        mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/store-brands").param("query", "lid")))
            .andExpect(jsonPath("$.storeBrands[0].name").value("Lidl"));
        mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/stores").param("query", "POZNAŃ")))
            .andExpect(jsonPath("$.items[0].id").value(storeId));
    }

    @Test
    void productCategoryCommandsReturnOnlyId() throws Exception {
        String categoryId = idOf(mockMvc.perform(json(as(userSub, post(CONTEXT_PATH + "/product-categories")), "{\"name\": \"Warzywa\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*", hasSize(1))));

        flush();
        Integer version = JsonPath.read(mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/product-categories")))
            .andReturn().getResponse().getContentAsString(), "$.items[0].version");
        String rename = "{\"version\": %d, \"name\": \"Owoce\"}".formatted(version);

        mockMvc.perform(json(as(userSub, put(CONTEXT_PATH + "/product-categories/" + categoryId)), rename))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*", hasSize(1)))
            .andExpect(jsonPath("$.id").value(categoryId));
        flush();
        mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/product-categories")))
            .andExpect(jsonPath("$.items[0].name").value("Owoce"));
        expectError(mockMvc.perform(json(as(userSub, put(CONTEXT_PATH + "/product-categories/" + categoryId)), rename)),
            409, "EXCEPTION.VERSION_CONFLICT");
        expectError(mockMvc.perform(json(as(userSub, post(CONTEXT_PATH + "/product-categories")), "{\"name\": \"owoce\"}")),
            400, "EXCEPTION.PRODUCT_CATEGORY_ALREADY_EXISTS");
    }

    @Test
    void uploadedReceiptCanBeReadEditedDownloadedAndDeleted() throws Exception {
        UUID userId = jdbcTemplate.queryForObject("SELECT id FROM app_user WHERE user_sub = ?", UUID.class, userSub);
        UUID walletId = createWallet(userId, "Home");
        UUID storeId = createStore("Biedronka", "ul. Testowa 1", "Poznań");
        byte[] receiptJson = readResource("receipts/biedronka/biedronka_e_receipt.json");
        String body = "{\"name\": \"Biedronka\", \"strategy\": \"BIEDRONKA_JSON\", \"walletId\": \"%s\", \"storeId\": \"%s\"}"
            .formatted(walletId, storeId);

        String receiptId = idOf(mockMvc.perform(as(userSub, multipart(CONTEXT_PATH + "/receipts")
                .file(new MockMultipartFile("file", "receipt.json", MediaType.APPLICATION_JSON_VALUE, receiptJson))
                .file(new MockMultipartFile("body", "", MediaType.APPLICATION_JSON_VALUE, body.getBytes()))))
            .andExpect(status().isCreated()));
        flush();

        String details = mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/receipts/" + receiptId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Biedronka"))
            .andExpect(jsonPath("$.walletId").value(walletId.toString()))
            .andExpect(jsonPath("$.store.brand").value("Biedronka"))
            .andExpect(jsonPath("$.revisions", hasSize(1)))
            .andExpect(jsonPath("$.revisions[0].resolver").value("BIEDRONKA_JSON"))
            .andExpect(jsonPath("$.fileGroups[0].fileType").value("JSON"))
            .andReturn().getResponse().getContentAsString();
        String revisionId = JsonPath.read(details, "$.revisions[0].id");
        String fileId = JsonPath.read(details, "$.fileGroups[0].files[0].id");

        mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/receipt-files/" + fileId)))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + ".json\""))
            .andExpect(content().bytes(receiptJson));

        mockMvc.perform(json(as(userSub, put(CONTEXT_PATH + "/receipts/" + receiptId)),
                "{\"version\": %d, \"name\": \" Zakupy \", \"description\": \"tydzień\", \"storeId\": \"%s\"}"
                    .formatted(JsonPath.<Integer>read(details, "$.version"), storeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*", hasSize(1)));
        String copyId = idOf(mockMvc.perform(json(as(userSub, post(CONTEXT_PATH + "/revisions/" + revisionId + "/copy")), "{}"))
            .andExpect(status().isCreated()), "$.revisionId");
        mockMvc.perform(json(as(userSub, put(CONTEXT_PATH + "/revisions/" + copyId)),
                "{\"version\": %d, \"totalPrice\": 330.75, \"paymentDate\": \"2026-05-22T10:04:00\", \"isPreferredRevision\": true, \"items\": []}"
                    .formatted(version("receipt_revision", UUID.fromString(copyId)))))
            .andExpect(status().isOk());
        flush();

        mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/receipts/" + receiptId)))
            .andExpect(jsonPath("$.name").value("Zakupy"))
            .andExpect(jsonPath("$.description").value("tydzień"))
            .andExpect(jsonPath("$.preferredRevision.id").value(copyId))
            .andExpect(jsonPath("$.preferredRevision.paymentDate").value("2026-05-22T10:04:00"))
            .andExpect(jsonPath("$.preferredRevision.items", hasSize(0)));
        mockMvc.perform(as(registerOtherUser(), get(CONTEXT_PATH + "/receipt-files/" + fileId)))
            .andExpect(status().isNotFound());

        mockMvc.perform(as(userSub, delete(CONTEXT_PATH + "/revisions/" + copyId))).andExpect(status().isNoContent());
        mockMvc.perform(as(userSub, delete(CONTEXT_PATH + "/receipts/" + receiptId))).andExpect(status().isNoContent());
        flush();
        expectError(mockMvc.perform(as(userSub, get(CONTEXT_PATH + "/receipts/" + receiptId))), 400, "EXCEPTION.RECEIPT_NOT_FOUND");
    }

    @Test
    void revisionUpdateWithoutItemsIsRejected() throws Exception {
        expectError(mockMvc.perform(json(as(userSub, put(CONTEXT_PATH + "/revisions/" + UUID.randomUUID())), "{\"totalPrice\": 1}")),
            400, "EXCEPTION.INVALID_REQUEST");
    }

    private String registerOtherUser() throws Exception {
        String sub = "http-stranger-" + UUID.randomUUID();
        mockMvc.perform(as(sub, post(CONTEXT_PATH + "/users/sync/me"))).andExpect(status().isOk());
        return sub;
    }

    private static MockHttpServletRequestBuilder api(MockHttpServletRequestBuilder request) {
        return request.contextPath(CONTEXT_PATH);
    }

    private static MockHttpServletRequestBuilder as(String sub, MockHttpServletRequestBuilder request) {
        return api(request).header(HttpHeaders.AUTHORIZATION, "Bearer " + sub);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static String idOf(ResultActions result) throws Exception {
        return idOf(result, "$.id");
    }

    private static String idOf(ResultActions result, String path) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
    }

    private static void expectError(ResultActions result, int status, String code) throws Exception {
        result.andExpect(status().is(status))
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.status").value(status));
    }
}
