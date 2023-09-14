package io.adobe.cloudmanager.impl;

import java.util.concurrent.TimeUnit;

import com.adobe.aio.feign.AIOHeaderInterceptor;
import com.adobe.aio.ims.feign.AuthInterceptor;
import com.adobe.aio.workspace.Workspace;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;

import static com.adobe.aio.util.feign.FeignUtil.*;

public class FeignUtil {

  private FeignUtil() {

  }

  public static Feign.Builder getBuilder(Workspace workspace) {
    ObjectMapper mapper = JsonMapper.builder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
        .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
        .addModule(new JavaTimeModule())
        .build();

    RequestInterceptor authInterceptor = AuthInterceptor.builder().workspace(workspace).build();
    RequestInterceptor aioHeaderInterceptor = AIOHeaderInterceptor.builder().workspace(workspace).build();
    Request.Options options = new Request.Options(DEFAULT_CONNECT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, DEFAULT_READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, true);
    return Feign.builder()
        .client(new OkHttpClient())
        .logger(new Slf4jLogger())
        .logLevel(Logger.Level.BASIC)
        .requestInterceptor(authInterceptor)
        .requestInterceptor(aioHeaderInterceptor)
        .encoder(new JacksonEncoder(mapper))
        .decoder(new JacksonDecoder(mapper))
        .options(options);
  }
}
