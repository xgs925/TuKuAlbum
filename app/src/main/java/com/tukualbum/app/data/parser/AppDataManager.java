package com.tukualbum.app.data.parser;



import com.tukualbum.app.data.parser.model.BaseResult;
import com.tukualbum.app.data.parser.model.MeiZiTu;
import com.tukualbum.app.data.parser.model.Mm99;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * @author flymegoc
 * @date 2017/11/22
 * @describe
 */

@Singleton
public class AppDataManager{

    private final ApiHelper mApiHelper;

    @Inject
    AppDataManager(ApiHelper mApiHelper) {
        this.mApiHelper = mApiHelper;
    }


    public Observable<BaseResult<List<MeiZiTu>>> listMeiZiTu(String tag, int page, boolean pullToRefresh) {
        return mApiHelper.listMeiZiTu(tag, page, pullToRefresh);
    }

    public Observable<List<String>> meiZiTuImageList(int id, boolean pullToRefresh) {
        return mApiHelper.meiZiTuImageList(id, pullToRefresh);
    }

    public Observable<BaseResult<List<Mm99>>> list99Mm(String category, int page, boolean cleanCache) {
        return mApiHelper.list99Mm(category, page, cleanCache);
    }

    public Observable<List<String>> mm99ImageList(int id, String imageUrl, boolean pullToRefresh) {
        return mApiHelper.mm99ImageList(id, imageUrl, pullToRefresh);
    }


}
