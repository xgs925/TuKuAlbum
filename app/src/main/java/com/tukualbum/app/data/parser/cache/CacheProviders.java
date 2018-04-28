package com.tukualbum.app.data.parser.cache;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.DynamicKeyGroup;
import io.rx_cache2.EvictDynamicKey;
import io.rx_cache2.EvictDynamicKeyGroup;
import io.rx_cache2.EvictProvider;
import io.rx_cache2.LifeCache;
import io.rx_cache2.ProviderKey;
import io.rx_cache2.Reply;

/**
 * RxJavaCache 缓存
 *
 * @author flymegoc
 * @date 2017/11/18
 */

public interface CacheProviders {
    /**
     * 缓存自动过期时间15分钟
     */
    int CACHE_TIME = 15;
    /**
     * 妹子图分类
     *
     * @param observable           ob
     * @param dynamicKeyGroup      页码分类
     * @param evictDynamicKeyGroup 缓存控制
     * @return ob
     */
    @ProviderKey("mei_zi_tu")
    @LifeCache(duration = CACHE_TIME, timeUnit = TimeUnit.MINUTES)
    Observable<Reply<String>> meiZiTu(Observable<String> observable, DynamicKeyGroup dynamicKeyGroup, EvictDynamicKeyGroup evictDynamicKeyGroup);

    /**
     * 妹子图浏览，永不过期
     *
     * @param observable ob
     * @return ob
     */
    @ProviderKey("mei_zi_tu_pic_list")
    Observable<Reply<String>> meiZiTu(Observable<String> observable, DynamicKey dynamicKey, EvictDynamicKey evictDynamicKey);



    @ProviderKey("cache_v113")
    @LifeCache(duration = CACHE_TIME, timeUnit = TimeUnit.MINUTES)
    Observable<Reply<String>> cacheWithLimitTime(Observable<String> observable, DynamicKey dynamicKey, EvictDynamicKey evictDynamicKey);

    @ProviderKey("cache_v113")
    Observable<Reply<String>> cacheWithNoLimitTime(Observable<String> observable, DynamicKey dynamicKey, EvictDynamicKey evictDynamicKey);

    @ProviderKey("cache_v113")
    @LifeCache(duration = CACHE_TIME, timeUnit = TimeUnit.MINUTES)
    Observable<Reply<String>> cacheWithLimitTime(Observable<String> observable, DynamicKeyGroup dynamicKeyGroup, EvictDynamicKeyGroup evictDynamicKeyGroup);

    @ProviderKey("cache_v113")
    Observable<Reply<String>> cacheWithNoLimitTime(Observable<String> observable, DynamicKeyGroup dynamicKeyGroup, EvictDynamicKeyGroup evictDynamicKeyGroup);
}
