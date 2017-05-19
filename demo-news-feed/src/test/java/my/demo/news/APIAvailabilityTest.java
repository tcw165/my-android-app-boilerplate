package my.demo.news;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import my.demo.news.net.INewsServiceApi;
import my.demo.news.net.StringResponseFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class APIAvailabilityTest {

    private INewsServiceApi mService;

    @Before
    public void setupService() {
        MockitoAnnotations.initMocks(this);

        // The service API.
        final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();
        final Retrofit factory = new Retrofit.Builder()
            .baseUrl("http://api.myjson.com/")
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(StringResponseFactory.create())
            .build();
        mService = factory.create(INewsServiceApi.class);
    }

    @Test
    public void testApiAvailability() throws Exception {
        // Make sure to get any non-empty string.
        Assert.assertTrue(
            mService
                .getNews()
                .blockingLast()
                .length() > 0);
    }
}
