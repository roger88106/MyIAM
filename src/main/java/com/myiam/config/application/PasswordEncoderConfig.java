package com.myiam.config.application;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * パスワードエンコーダーの設定
 */
@Configuration
class PasswordEncoderConfig {
    /**
     * パスワードエンコーダーの設定。
     *
     * @return パスワードエンコーダー
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 暗号化アルゴリズムのアップグレードを防ぐため、NonUpgradingPasswordEncoderを使用
        return new NonUpgradingPasswordEncoder(
                // DelegatingPasswordEncoder を使用し、複数の暗号化アルゴリズムをサポート
                PasswordEncoderFactories.createDelegatingPasswordEncoder());
    }

    /**
     * 暗号化アルゴリズムのアップグレードをロックするためのPasswordEncoderラッピング
     */
    private record NonUpgradingPasswordEncoder(PasswordEncoder encoder) implements PasswordEncoder {

        /** アップグレードを防ぐため、upgradeEncodingを上書きする */
        @Override
        public boolean upgradeEncoding(@Nullable String encodedPassword) {
            return false;
        }

        /** encodeをそのまま使用 */
        @Override
        public @Nullable String encode(@Nullable CharSequence rawPassword) {
            return encoder.encode(rawPassword);
        }

        /** DelegatingPasswordEncoder.matchesをそのまま使用 */
        @Override
        public boolean matches(@Nullable CharSequence rawPassword, @Nullable String encodedPassword) {
            return encoder.matches(rawPassword, encodedPassword);
        }
    }
}
