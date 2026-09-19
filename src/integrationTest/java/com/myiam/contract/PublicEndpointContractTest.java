package com.myiam.contract;

import com.myiam.TestcontainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * 公開エンドポイント契約テスト。<br />
 * コントローラーに登録された全マッピングを匿名で叩き、
 * 「公開と宣言したもの以外は 401 / 403 で拒否される」ことを保証する。<br />
 * 公開エンドポイントを増やしたら {@link #PUBLIC} に追加すること（＝レビューで見える）。
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
@AutoConfigureMockMvc
class PublicEndpointContractTest {

    /**
     * 匿名アクセスを許可するエンドポイント（method + パターン）。<br />
     * ここに無いものは全て認証必須。
     */
    private static final Set<String> PUBLIC = Set.of(
            "GET /login",
            "POST /api/users/register",
            "/error"
    );

    @Autowired
    MockMvc mockMvc;

    /** MVC のマッピングのみ（actuator の controllerEndpointHandlerMapping は対象外） */
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping handlerMapping;

    @TestFactory
    @DisplayName("コントローラーの全マッピングは、公開宣言されたもの以外は匿名で拒否される")
    Stream<DynamicTest> everyMappingIsProtectedUnlessDeclaredPublic() {
        return handlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(PublicEndpointContractTest::expand)
                .map(endpoint -> DynamicTest.dynamicTest(endpoint.label(), () -> {
                    int status = mockMvc.perform(request(endpoint.method(), endpoint.concretePath()))
                            .andReturn().getResponse().getStatus();

                    if (endpoint.isPublic()) {
                        assertThat(status)
                                .as("%s は公開エンドポイントなので 401/403 であってはならない", endpoint.label())
                                .isNotIn(401, 403);
                    } else {
                        assertThat(status)
                                .as("%s は公開宣言されていないので匿名では 401 か 403 でなければならない", endpoint.label())
                                .isIn(401, 403);
                    }
                }));
    }

    // ============================== ヘルパー ==============================

    /** マッピング情報を (method, pattern) の組に展開する。method 未指定は GET とみなす */
    private static Stream<Endpoint> expand(RequestMappingInfo info) {
        var methods = info.getMethodsCondition().getMethods();
        var patterns = info.getPathPatternsCondition() != null
                ? info.getPathPatternsCondition().getPatternValues()
                : Set.<String>of();

        return patterns.stream().flatMap(pattern -> methods.isEmpty()
                ? Stream.of(new Endpoint(HttpMethod.GET, pattern, true))
                : methods.stream().map(m -> new Endpoint(HttpMethod.valueOf(m.name()), pattern, false)));
    }

    /**
     * @param method       HTTP メソッド
     * @param pattern      マッピングのパターン（{userId} 等を含む）
     * @param methodAgnostic method 指定の無いマッピングか
     */
    private record Endpoint(HttpMethod method, String pattern, boolean methodAgnostic) {

        String label() {
            return methodAgnostic ? pattern : method + " " + pattern;
        }

        boolean isPublic() {
            return PUBLIC.contains(label());
        }

        /** パス変数をダミー値に置換した実 URL。匿名は filter 層で弾かれるため値の内容は問わない */
        String concretePath() {
            return pattern
                    .replace("{userId}", UUID.randomUUID().toString())
                    .replaceAll("\\{[^}]+}", "dummy");
        }
    }
}
