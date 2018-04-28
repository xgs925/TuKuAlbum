package com.tukualbum.app.data.parser;

import android.content.Context;

import com.tukualbum.app.App;
import com.tukualbum.app.data.parser.apiservice.MeiZiTuServiceApi;
import com.tukualbum.app.data.parser.apiservice.Mm99ServiceApi;
import com.tukualbum.app.data.parser.cache.CacheProviders;
import com.tukualbum.app.data.parser.model.BaseResult;
import com.tukualbum.app.data.parser.model.MeiZiTu;
import com.tukualbum.app.data.parser.model.Mm99;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.DynamicKeyGroup;
import io.rx_cache2.EvictDynamicKey;
import io.rx_cache2.EvictDynamicKeyGroup;
import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author flymegoc
 * @date 2018/3/4
 */

public class AppApiHelper implements ApiHelper {

    private static final String TAG = AppApiHelper.class.getSimpleName();
    private CacheProviders cacheProviders;

    private MeiZiTuServiceApi meiZiTuServiceApi;
    private Mm99ServiceApi mm99ServiceApi;
    OkHttpClient okHttpClient;
    private static AppApiHelper instance = new AppApiHelper();

    public static AppApiHelper get() {
        return instance;
    }

    public AppApiHelper() {
        meiZiTuServiceApi = getMeiZiTuServiceApi();
        mm99ServiceApi = getMm99ServiceApi();
        cacheProviders=providesCacheProviders(App.getInstance());
    }

    OkHttpClient providesOkHttpClient() {
        if (okHttpClient != null) return okHttpClient;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        okHttpClient = builder.build();
        return okHttpClient;
    }
    CacheProviders providesCacheProviders(Context context) {
        File cacheDir = AppCacheUtils.getRxCacheDir(context);
        return new RxCache.Builder()
                .persistence(cacheDir, new GsonSpeaker())
                .using(CacheProviders.class);
    }
    public MeiZiTuServiceApi getMeiZiTuServiceApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(providesOkHttpClient())
                .baseUrl(Api.APP_MEIZITU_DOMAIN)
                .addConverterFactory(ScalarsConverterFactory.create())//请求的结果转为实体类
                //适配RxJava2.0,RxJava1.x则为RxJavaCallAdapterFactory.create()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        MeiZiTuServiceApi apiService = retrofit.create(MeiZiTuServiceApi.class);
        return apiService;
    }

    public Mm99ServiceApi getMm99ServiceApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(providesOkHttpClient())
                .baseUrl(Api.APP_MEIZITU_DOMAIN)
                .addConverterFactory(ScalarsConverterFactory.create())//请求的结果转为实体类
                //适配RxJava2.0,RxJava1.x则为RxJavaCallAdapterFactory.create()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        Mm99ServiceApi apiService = retrofit.create(Mm99ServiceApi.class);
        return apiService;
    }


    @Override
    public Observable<BaseResult<List<MeiZiTu>>> listMeiZiTu(String tag, int page, boolean pullToRefresh) {
        switch (tag) {
            case "index":
                return action(meiZiTuServiceApi.meiZiTuIndex(page), tag, page, pullToRefresh);
            case "hot":
                return action(meiZiTuServiceApi.meiZiTuHot(page), tag, page, pullToRefresh);
            case "best":
                return action(meiZiTuServiceApi.meiZiTuBest(page), tag, page, pullToRefresh);
            case "japan":
                return action(meiZiTuServiceApi.meiZiTuJapan(page), tag, page, pullToRefresh);
            case "taiwan":
                return action(meiZiTuServiceApi.meiZiTuJaiwan(page), tag, page, pullToRefresh);
            case "xinggan":
                return action(meiZiTuServiceApi.meiZiTuSexy(page), tag, page, pullToRefresh);
            case "mm":
                return action(meiZiTuServiceApi.meiZiTuMm(page), tag, page, pullToRefresh);
            default:
                return null;
        }
    }

    @Override
    public Observable<List<String>> meiZiTuImageList(int id, boolean pullToRefresh) {
        return cacheProviders.meiZiTu(meiZiTuServiceApi.meiZiTuImageList(id), new DynamicKey(id), new EvictDynamicKey(pullToRefresh))
                .map(stringReply -> stringReply.getData())
                .map(s -> {
                    BaseResult<List<String>> baseResult = ParseMeiZiTu.parsePicturePage(s);
                    return baseResult.getData();
                });
    }

    @Override
    public Observable<BaseResult<List<Mm99>>> list99Mm(String category, final int page, boolean cleanCache) {
        String url = buildUrl(category, page);
        DynamicKeyGroup dynamicKeyGroup = new DynamicKeyGroup(category, page);
        EvictDynamicKeyGroup evictDynamicKeyGroup = new EvictDynamicKeyGroup(cleanCache);
        return cacheProviders.cacheWithLimitTime(mm99ServiceApi.imageList(url), dynamicKeyGroup, evictDynamicKeyGroup)
                .map(stringReply -> stringReply.getData())
                .map(s -> Parse99Mm.parse99MmList(s, page));
    }

    @Override
    public Observable<List<String>> mm99ImageList(int id, final String imageUrl, boolean pullToRefresh) {
        return cacheProviders.cacheWithNoLimitTime(mm99ServiceApi.imageLists("view", id), new DynamicKey(id), new EvictDynamicKey(pullToRefresh))
                .map(stringReply -> stringReply.getData())
                .map(s -> {
                    String[] tags = s.split(",");
                    List<String> stringList = new ArrayList<>();
                    for (int i = 0; i < tags.length; i++) {
                        stringList.add(imageUrl.replace("small/", "").replace(".jpg", "/" + (i + 1) + "-" + tags[i]) + ".jpg");
                    }
                    return stringList;
                });
    }


    private Observable<BaseResult<List<MeiZiTu>>> action(Observable<String> stringObservable, String tag, final int page, final boolean pullToRefresh) {
        DynamicKeyGroup dynamicKeyGroup = new DynamicKeyGroup(tag, page);
        EvictDynamicKeyGroup evictDynamicKeyGroup = new EvictDynamicKeyGroup(pullToRefresh);
        return cacheProviders.meiZiTu(stringObservable, dynamicKeyGroup, evictDynamicKeyGroup)
                .map(stringReply -> stringReply.getData())
                .map(s -> ParseMeiZiTu.parseMeiZiTuList(s, page));
    }

    private String buildUrl(String category, int page) {
        switch (category) {
            case "meitui":
                if (page == 1) {
                    return Api.APP_99_MM_DOMAIN + "meitui/";
                } else {
                    return Api.APP_99_MM_DOMAIN + "meitui/mm_1_" + page + ".html";
                }

            case "xinggan":
                if (page == 1) {
                    return Api.APP_99_MM_DOMAIN + "xinggan/";
                } else {
                    return Api.APP_99_MM_DOMAIN + "xinggan/mm_2_" + page + ".html";
                }

            case "qingchun":
                if (page == 1) {
                    return Api.APP_99_MM_DOMAIN + "qingchun/";
                } else {
                    return Api.APP_99_MM_DOMAIN + "qingchun/mm_3_" + page + ".html";
                }

            case "hot":
                if (page == 1) {
                    return Api.APP_99_MM_DOMAIN + "hot/";
                } else {
                    return Api.APP_99_MM_DOMAIN + "hot/mm_4_" + page + ".html";
                }

            default:
                return Api.APP_99_MM_DOMAIN;
        }
    }


}
