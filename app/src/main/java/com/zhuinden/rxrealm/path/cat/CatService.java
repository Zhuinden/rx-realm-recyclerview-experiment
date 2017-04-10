package com.zhuinden.rxrealm.path.cat;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public interface CatService {
    @GET("api/images/get?format=xml&results_per_page=20")
    Observable<CatsBO> getCats();
}
