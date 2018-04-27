package com.tukualbum.app.data.parser;



import com.tukualbum.app.data.parser.model.BaseResult;
import com.tukualbum.app.data.parser.model.MeiZiTu;
import com.tukualbum.app.data.parser.model.Mm99;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author flymegoc
 * @date 2018/3/4
 */

public interface ApiHelper {



    Observable<BaseResult<List<MeiZiTu>>> listMeiZiTu(String tag, int page, boolean pullToRefresh);

    Observable<List<String>> meiZiTuImageList(int id, boolean pullToRefresh);

    Observable<BaseResult<List<Mm99>>> list99Mm(String category, int page, boolean cleanCache);

    Observable<List<String>> mm99ImageList(int id, String imageUrl, boolean pullToRefresh);


}
