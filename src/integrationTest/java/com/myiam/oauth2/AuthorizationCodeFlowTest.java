package com.myiam.oauth2;

import com.myiam.TestcontainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OAuth2 Authorization Code + PKCE フローの統合テスト。<br />
 * 画面は経由せず、フォームログイン → authorize → token を MockMvc で直接叩く。
 * seed のテストユーザー（test@test.com）と public-client を使用する。
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
@AutoConfigureMockMvc
class AuthorizationCodeFlowTest {

    private static final String USER_EMAIL = "test@test.com";
    private static final String USER_PASSWORD = "password01";
    private static final String USER_ID = "cc2b8ab4-3a3e-4583-9926-9f8cdef13dd9";
    private static final String CLIENT_ID = "public-client";
    private static final String CLIENT_SECRET = "public-secret";
    private static final String REDIRECT_URI = "http://localhost:8081/login/oauth2/code/public-client";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    MockMvc mockMvc;

    // ============================== 正常系 ==============================

    @Nested
    @DisplayName("token 発行")
    class TokenIssuance {

        @Test
        @DisplayName("全 scope → permissions は subject の権限（5 個）、roles と email は access token に無い")
        void accessTokenCarriesPermissionsOnly() throws Exception {
            Tokens tokens = login().authorizeAndExchange(
                    "openid profile email admin:access user:read user:write user:delete role:read role:assign permission:read");

            JsonNode access = payloadOf(tokens.accessToken());
            assertThat(access.get("sub").asString()).isEqualTo(USER_ID);
            assertThat(stringList(access.get("permissions")))
                    .containsExactlyInAnyOrder("admin:access", "user:read", "user:write", "role:read", "permission:read");
            assertThat(access.has("roles")).as("roles は access token に入れない").isFalse();
            assertThat(access.has("email")).as("email は access token に入れない").isFalse();
        }

        @Test
        @DisplayName("id_token には roles（停用ロールは除く）と profile claim が入る")
        void idTokenCarriesRolesAndProfile() throws Exception {
            Tokens tokens = login().authorizeAndExchange("openid profile email");

            JsonNode id = payloadOf(tokens.idToken());
            assertThat(stringList(id.get("roles"))).containsExactlyInAnyOrder("USER_MANAGER", "VIEWER");
            assertThat(id.get("email").asString()).isEqualTo(USER_EMAIL);
            assertThat(id.get("family_name").asString()).isEqualTo("テスト");
            assertThat(id.has("permissions")).isFalse();
        }

        @Test
        @DisplayName("client 側 scope で絞ると permissions は交集になる")
        void scopeNarrowsPermissions() throws Exception {
            Tokens tokens = login().authorizeAndExchange("openid user:read");

            assertThat(stringList(payloadOf(tokens.accessToken()).get("permissions")))
                    .containsExactly("user:read");
        }

        @Test
        @DisplayName("permission 系 scope を要求しない → permissions は空配列（claim 自体は存在する）")
        void noPermissionScopeGivesEmptyArray() throws Exception {
            Tokens tokens = login().authorizeAndExchange("openid");

            JsonNode permissions = payloadOf(tokens.accessToken()).get("permissions");
            assertThat(permissions).isNotNull();
            assertThat(permissions.isArray()).isTrue();
            assertThat(permissions).isEmpty();
        }
    }

    // ============================== refresh token ==============================

    @Nested
    @DisplayName("refresh token")
    class RefreshToken {

        @Test
        @DisplayName("rotation：refresh すると新しい RT が返り、古い RT は invalid_grant")
        void refreshRotatesToken() throws Exception {
            Tokens first = login().authorizeAndExchange("openid offline_access user:read");

            Tokens second = refresh(first.refreshToken()).expectOk();
            assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());

            refresh(first.refreshToken()).expectInvalidGrant();
        }

        @Test
        @DisplayName("reuse detection：古い RT を再利用すると、正当な最新の RT まで失効する")
        void reuseRevokesWholeFamily() throws Exception {
            Tokens t1 = login().authorizeAndExchange("openid offline_access user:read");
            Tokens t2 = refresh(t1.refreshToken()).expectOk();
            Tokens t3 = refresh(t2.refreshToken()).expectOk();

            // t1 の RT を再利用（攻撃者の想定）
            refresh(t1.refreshToken()).expectInvalidGrant();

            // 連座失効：本来は有効だった t3 の RT も使えない
            refresh(t3.refreshToken()).expectInvalidGrant();
        }
    }

    // ============================== lockout ==============================

    @Nested
    @DisplayName("lockout")
    class Lockout {

        @Test
        @DisplayName("誤ったパスワード 4 回までは BadCredentials、5 回目でロック、以降は正しいパスワードでも Locked")
        void locksAfterFiveFailures() throws Exception {
            // ※ seed の test ユーザーをロックすると他テストに影響するため、admin ユーザーで実施
            //    （コンテナは JVM 内で共有される。admin は他テストで使っていない）
            String email = "admin@test.com";

            for (int i = 1; i <= 4; i++) {
                assertThat(failureOf(loginResult(email, "wrong-" + i)))
                        .as("%d 回目の失敗", i)
                        .isInstanceOf(BadCredentialsException.class);
            }

            // 5 回目：閾値到達でその場でロック
            assertThat(failureOf(loginResult(email, "wrong-5"))).isInstanceOf(LockedException.class);

            // 正しいパスワードでもロック中として弾かれる
            assertThat(failureOf(loginResult(email, USER_PASSWORD))).isInstanceOf(LockedException.class);
        }

        /** 認証失敗ハンドラーが session に積んだ例外を取り出す */
        private AuthenticationException failureOf(MvcResult result) {
            assertThat(result.getResponse().getRedirectedUrl()).endsWith("/login");
            Object ex = result.getRequest().getSession(false).getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            assertThat(ex).isInstanceOf(AuthenticationException.class);
            return (AuthenticationException) ex;
        }
    }

    // ============================== フローのヘルパー ==============================

    /** フォームログインし、認証済み session を返す */
    private Session login() throws Exception {
        MvcResult result = loginResult(USER_EMAIL, USER_PASSWORD);
        assertThat(result.getResponse().getRedirectedUrl())
                .as("ログイン成功時は保存済みリクエスト（無ければ /）へ")
                .doesNotEndWith("/login");
        return new Session((MockHttpSession) result.getRequest().getSession(false));
    }

    private MvcResult loginResult(String email, String password) throws Exception {
        return mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    private TokenResponse refresh(String refreshToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", refreshToken)
                        .param("client_id", CLIENT_ID)
                        .param("client_secret", CLIENT_SECRET))
                .andReturn();
        return new TokenResponse(result);
    }

    /** 認証済み session 上で authorize → token を行う */
    private final class Session {
        private final MockHttpSession session;

        Session(MockHttpSession session) {
            this.session = session;
        }

        Tokens authorizeAndExchange(String scope) throws Exception {
            Pkce pkce = Pkce.generate();

            // ※ SAS の authorize endpoint は GET のクエリ文字列を直接解析するため、.param() ではなく URI に載せる
            //    （MockMvc が URI からクエリ文字列とパラメータマップの両方を組み立てる。encode は MockMvc 側に任せる）
            URI authorizeUri = UriComponentsBuilder.fromPath("/oauth2/authorize")
                    .queryParam("response_type", "code")
                    .queryParam("client_id", CLIENT_ID)
                    .queryParam("redirect_uri", REDIRECT_URI)
                    .queryParam("scope", scope)
                    .queryParam("state", "it")
                    .queryParam("code_challenge", pkce.challenge())
                    .queryParam("code_challenge_method", "S256")
                    .build()
                    .toUri();

            MvcResult authorize = mockMvc.perform(get(authorizeUri).session(session))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();

            String location = authorize.getResponse().getRedirectedUrl();
            assertThat(location).as("authorize は redirect_uri へ code 付きで戻る").startsWith(REDIRECT_URI);
            String code = queryParam(location, "code");

            MvcResult token = mockMvc.perform(post("/oauth2/token")
                            .param("grant_type", "authorization_code")
                            .param("code", code)
                            .param("redirect_uri", REDIRECT_URI)
                            .param("client_id", CLIENT_ID)
                            .param("client_secret", CLIENT_SECRET)
                            .param("code_verifier", pkce.verifier()))
                    .andReturn();

            return new TokenResponse(token).expectOk();
        }
    }

    /** /oauth2/token のレスポンス */
    private record TokenResponse(MvcResult result) {

        Tokens expectOk() throws Exception {
            assertThat(result.getResponse().getStatus())
                    .as("token endpoint: %s", result.getResponse().getContentAsString())
                    .isEqualTo(200);
            JsonNode body = JSON.readTree(result.getResponse().getContentAsString());
            return new Tokens(
                    body.get("access_token").asString(),
                    body.has("refresh_token") ? body.get("refresh_token").asString() : null,
                    body.has("id_token") ? body.get("id_token").asString() : null);
        }

        void expectInvalidGrant() throws Exception {
            assertThat(result.getResponse().getStatus()).isEqualTo(400);
            JsonNode body = JSON.readTree(result.getResponse().getContentAsString());
            assertThat(body.get("error").asString()).isEqualTo("invalid_grant");
        }
    }

    private record Tokens(String accessToken, String refreshToken, String idToken) {
    }

    private record Pkce(String verifier, String challenge) {
        static Pkce generate() throws Exception {
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
            return new Pkce(verifier, challenge);
        }
    }

    // ============================== 解析ヘルパー ==============================

    private static JsonNode payloadOf(String jwt) {
        String payload = jwt.split("\\.")[1];
        return JSON.readTree(Base64.getUrlDecoder().decode(payload));
    }

    private static List<String> stringList(JsonNode array) {
        return array.valueStream().map(JsonNode::asString).collect(Collectors.toList());
    }

    private static String queryParam(String url, String name) {
        Map<String, String> params = Arrays.stream(URI.create(url).getRawQuery().split("&"))
                .map(kv -> kv.split("=", 2))
                .collect(Collectors.toMap(kv -> kv[0], kv -> URLDecoder.decode(kv[1], StandardCharsets.UTF_8)));
        assertThat(params).containsKey(name);
        return params.get(name);
    }
}
