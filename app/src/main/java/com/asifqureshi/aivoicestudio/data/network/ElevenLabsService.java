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
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * Retrofit service interface for ElevenLabs AI Voice API.
 * Handles text-to-speech generation, streaming, voice discovery, and model settings.
 */
public interface ElevenLabsService {

    /**
     * Primary Text-to-Speech generation endpoint.
     * Generates audio from text using specified voice and voice settings.
     *
     * @param voiceId The ID of the ElevenLabs voice (e.g., 21m00Tcm4TlvDq8ikWAM)
     * @param outputFormat Format of the returned audio (e.g., mp3_44100_128, mp3_44100_192, pcm_16000)
     * @param apiKey ElevenLabs API authentication key (xi-api-key header)
     * @param request Payload containing text prompt, model_id, and VoiceSettings
     * @return Streamed binary audio response (audio/mpeg)
     */
    @Streaming
    @Headers({"Accept: audio/mpeg", "Content-Type: application/json"})
    @POST("v1/text-to-speech/{voice_id}")
    Call<ResponseBody> generateTextToSpeech(
            @Path("voice_id") String voiceId,
            @Query("output_format") String outputFormat,
            @Header("xi-api-key") String apiKey,
            @Body GenerationRequest request
    );

    /**
     * Simplified convenience overload for Text-to-Speech generation.
     */
    @Streaming
    @Headers({"Accept: audio/mpeg", "Content-Type: application/json"})
    @POST("v1/text-to-speech/{voice_id}")
    Call<ResponseBody> generateSpeech(
            @Path("voice_id") String voiceId,
            @Header("xi-api-key") String apiKey,
            @Body GenerationRequest request
    );

    /**
     * Real-time streaming Text-to-Speech endpoint with low latency optimization.
     *
     * @param voiceId The ID of the ElevenLabs voice
     * @param outputFormat Audio format query parameter
     * @param optimizeStreamingLatency Latency level from 0 (default) to 4 (maximum latency optimization)
     * @param apiKey ElevenLabs API authentication key
     * @param request Text-to-speech payload
     */
    @Streaming
    @Headers({"Accept: audio/mpeg", "Content-Type: application/json"})
    @POST("v1/text-to-speech/{voice_id}/stream")
    Call<ResponseBody> streamTextToSpeech(
            @Path("voice_id") String voiceId,
            @Query("output_format") String outputFormat,
            @Query("optimize_streaming_latency") Integer optimizeStreamingLatency,
            @Header("xi-api-key") String apiKey,
            @Body GenerationRequest request
    );

    /**
     * Retrieves all available ElevenLabs shared and premade voices.
     */
    @GET("v1/voices")
    Call<JsonObject> getVoices(
            @Header("xi-api-key") String apiKey
    );

    /**
     * Retrieves current user subscription details, tier, and remaining character quota.
     */
    @GET("v1/user/subscription")
    Call<JsonObject> getUserSubscription(
            @Header("xi-api-key") String apiKey
    );

    /**
     * Creates custom voice previews from text descriptions using Voice Design.
     */
    @POST("v1/text-to-voice/create-previews")
    Call<JsonObject> createVoicePreviews(
            @Header("xi-api-key") String apiKey,
            @Body RequestBody body
    );

    /**
     * Adds an instant cloned voice from an authorized audio sample.
     */
    @Multipart
    @POST("v1/voices/add")
    Call<JsonObject> addClonedVoice(
            @Header("xi-api-key") String apiKey,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part audioFile
    );
}
