package my.demo.news.net;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface INewsServiceApi {

    /**
     * Load news feed from the given URL.
     *
     * @return {@link Observable} of the response body, which is usually a JSON.
     */
    @GET("/bins/nl6jh")
    Observable<String> getNews();
}
