package com.asifqureshi.aivoicestudio.data.network;

import com.asifqureshi.aivoicestudio.data.models.OpenRouterRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface OpenRouterService {
    @Streaming
    @Headers({"Accept: audio/mpeg", "Content-Type: application/json"})
    @POST("v1/openrouter/audio/speech")
    Call<ResponseBody> generateOpenRouterSpeech(
            @Header("Authorization") String authorization,
            @Body OpenRouterRequest request
    );
}
