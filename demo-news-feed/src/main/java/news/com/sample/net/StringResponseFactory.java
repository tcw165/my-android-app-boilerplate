package news.com.sample.net;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class StringResponseFactory {

    private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain");

    public static Converter.Factory create() {
        return new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                                    Annotation[] annotations,
                                                                    Retrofit retrofit) {
                if (String.class.equals(type)) {
                    return new Converter<ResponseBody, String>() {
                        @Override
                        public String convert(ResponseBody value) throws IOException {
                            return value.string();
                        }
                    };
                } else {
                    return null;
                }
            }

            @Override
            public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                                  Annotation[] parameterAnnotations,
                                                                  Annotation[] methodAnnotations,
                                                                  Retrofit retrofit) {
                if (String.class.equals(type)) {
                    return new Converter<String, RequestBody>() {
                        @Override
                        public RequestBody convert(String value) throws IOException {
                            return RequestBody.create(MEDIA_TYPE, value);
                        }
                    };
                } else {
                    return null;
                }
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////


    private StringResponseFactory() {
        // PREVENT USER FROM INSTANTIATING IT.
    }
}
