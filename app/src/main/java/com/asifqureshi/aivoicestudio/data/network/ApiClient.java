package com.asifqureshi.aivoicestudio.data.network;

import android.content.Context;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String DEFAULT_ELEVENLABS_BASE_URL = "https://api.elevenlabs.io/";
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = "";

    public static synchronized ElevenLabsApiService getService(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);
        String baseUrl = prefs.getBackendUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = DEFAULT_ELEVENLABS_BASE_URL;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        if (retrofit == null || !currentBaseUrl.equals(baseUrl)) {
            currentBaseUrl = baseUrl;

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(ElevenLabsApiService.class);
    }

    public static synchronized void resetClient() {
        retrofit = null;
        currentBaseUrl = "";
    }
}
