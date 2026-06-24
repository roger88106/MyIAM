package com.spring.modulith;

import com.myiam.MyIamApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Spring Modulith のモジュール構造を検証するテストクラス。
 */
class ModulithTest {

    /**
     * モジュールの依存関係と境界を検証する。
     */
    @Test
    void verifyModules() {
        // すべてのモジュール情報を解析して出力した後、アーキテクチャの検証を実行する
        ApplicationModules modules = ApplicationModules.of(MyIamApplication.class);
        modules.forEach(System.out::println);
        modules.verify();
    }
}
