package com.myiam.contract;

import com.myiam.TestcontainersConfig;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.myiam.jooq.permission.Tables.PERMISSIONS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 権限カタログ契約テスト。<br />
 * コードが要求する権限文字列（{@code hasAuthority('...')}）が、
 * DB の権限カタログ（{@code permission.permissions}、seed 由来）に必ず存在することを保証する。<br />
 * 綴りの食い違いは「誰も通れない API」として静かに壊れるため、ここで検出する。
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
class PermissionCatalogContractTest {

    /** {@code hasAuthority('xxx')} から xxx を抜く */
    private static final Pattern HAS_AUTHORITY = Pattern.compile("hasAuthority\\('([^']+)'\\)");

    /**
     * filter chain 側（{@code .hasAuthority("...")}）で要求している権限。<br />
     * リフレクションで拾えないため手動列挙。増やしたらここにも追加すること。
     */
    private static final Set<String> REQUIRED_BY_FILTER_CHAIN = Set.of(
            "actuator:read"
    );

    /** MVC のマッピングのみ（actuator の controllerEndpointHandlerMapping は対象外） */
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping handlerMapping;

    @Autowired
    DSLContext dsl;

    @Test
    @DisplayName("コードが要求する全ての権限は、権限カタログに存在する")
    void everyRequiredPermissionExistsInCatalog() {
        Set<String> required = requiredPermissions();
        Set<String> catalog = catalogPermissions();

        assertThat(required)
                .as("要求している権限 %s のうち、カタログ %s に無いもの", required, catalog)
                .isSubsetOf(catalog);
    }

    @Test
    @DisplayName("（情報）カタログにあるがコードでは要求していない権限")
    void reportUnusedCatalogPermissions() {
        Set<String> unused = new TreeSet<>(catalogPermissions());
        unused.removeAll(requiredPermissions());

        // フロント用（admin:access）や将来用（role:read）は未使用でも正当なので失敗にはしない
        System.out.println("[contract] unused catalog permissions: " + unused);
    }

    // ============================== ヘルパー ==============================

    private Set<String> requiredPermissions() {
        Set<String> required = new TreeSet<>(REQUIRED_BY_FILTER_CHAIN);

        handlerMapping.getHandlerMethods().values().forEach(handler -> {
            PreAuthorize pre = AnnotatedElementUtils.findMergedAnnotation(handler.getMethod(), PreAuthorize.class);
            if (pre == null) {
                return;
            }
            Matcher m = HAS_AUTHORITY.matcher(pre.value());
            while (m.find()) {
                required.add(m.group(1));
            }
        });

        return required;
    }

    private Set<String> catalogPermissions() {
        return new TreeSet<>(dsl.select(PERMISSIONS.PERMISSION)
                .from(PERMISSIONS)
                .fetchSet(PERMISSIONS.PERMISSION));
    }
}
