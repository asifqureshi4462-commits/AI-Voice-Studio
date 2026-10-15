package com.asifqureshi.aivoicestudio.data.network;

import com.asifqureshi.aivoicestudio.data.models.GenerationRequest;
import com.google.gson.JsonObject;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ElevenLabsApiService {

    @GET("v1/voices")
    Call<JsonObject> getVoices(@Header("xi-api-key") String apiKey);

    @Streaming
    @POST("v1/text-to-speech/{voice_id}")
    Call<ResponseBody> generateSpeech(
            @Path("voice_id") String voiceId,
            @Query("output_format") String outputFormat,
            @Header("xi-api-key") String apiKey,
            @Body GenerationRequest request
    );

    @GET("v1/user/subscription")
    Call<JsonObject> getUserSubscription(@Header("xi-api-key") String apiKey);

    @POST("v1/text-to-voice/create-previews")
    Call<JsonObject> createVoicePreviews(
            @Header("xi-api-key") String apiKey,
            @Body RequestBody body
    );

    @Multipart
    @POST("v1/voices/add")
    Call<JsonObject> addClonedVoice(
            @Header("xi-api-key") String apiKey,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part audioFile
    );
}
