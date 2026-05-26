package com.myiam.microservices.auth.authorization.client;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * RegisteredClientRepository のキャッシュラップ。
 */
@RequiredArgsConstructor
class CachingRegisteredClientRepository implements RegisteredClientRepository {

    /** デリゲート対象のリポジトリ */
    private final RegisteredClientRepository delegate;

    /** 登録クライアントキャッシュ
     * <ul>
     *     <li>キー：テーブルID</li>
     *     <li>値：登録クライアント</li>
     * </ul> */
    private final Cache clientCache;

    /** 登録クライアントIDマップキャッシュ
     * <ul>
     *     <li>キー：クライアントID</li>
     *     <li>値：テーブルID</li>
     * </ul> */
    private final Cache clientIdMapCache;

    /**
     *
     * @param registeredClient the {@link RegisteredClient}
     */
    @Override
    public void save(@NonNull RegisteredClient registeredClient) {
        // 最初にデータベースへ永続化
        delegate.save(registeredClient);

        // 不整合を防ぐため、キャッシュをクリア
        clientCache.evict(registeredClient.getId());
        clientIdMapCache.evict(registeredClient.getClientId());
    }

    @Override
    @Nullable
    public RegisteredClient findById(@NonNull String id) {
        // キャッシュからクライアント取得する
        if (clientCache.get(id, RegisteredClient.class) instanceof RegisteredClient client) {
            return client;
        }

        // キャッシュがヒットしない場合、データベースからロード
        RegisteredClient client = delegate.findById(id);

        // 検索成功した場合、キャッシュに更新する
        if (client != null) {
        clientCache.put(id, client);
        clientIdMapCache.put(client.getClientId(), id);
        }

        return client;

    }

    @Override
    @Nullable
    public RegisteredClient findByClientId(@NonNull String clientId) {
        // キャッシュからクライアント取得する ※クライアントIDマップ経由
        if (clientIdMapCache.get(clientId, String.class) instanceof String id
                && clientCache.get(id, RegisteredClient.class) instanceof RegisteredClient client
        ) {
            return client;
        }

        // キャッシュミスの場合、データベースからロード
        RegisteredClient client = delegate.findByClientId(clientId);

        // 検索成功した場合、キャッシュに更新する
        if (client != null) {
            clientCache.put(client.getId(), client);
            clientIdMapCache.put(client.getClientId(), client.getId());
        }

        return client;
    }
}
