package com.bready.server.place.external;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.exception.PlaceErrorCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class KakaoPlaceClient {

    private static final Logger log = LoggerFactory.getLogger(KakaoPlaceClient.class);

    private final WebClient webClient;
    private final String restApiKey;

    public KakaoPlaceClient(
            @Qualifier("kakaoWebClient") WebClient webClient,
            @Value("${kakao.rest-api-key}") String restApiKey
    ) {
        this.webClient = webClient;
        this.restApiKey = restApiKey;
    }

    public String search(
            String keyword,
            Double latitude,
            Double longitude,
            Integer radius,
            String categoryGroupCode
    ) {
        if (keyword == null || keyword.isBlank()) {
            throw ApplicationException.from(PlaceErrorCase.INVALID_SEARCH_KEYWORD);
        }

        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .scheme("https")
                            .host("dapi.kakao.com")
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", keyword)
                            .queryParam("size", 15);

                    if (latitude != null && longitude != null) {
                        uriBuilder.queryParam("y", latitude)
                                .queryParam("x", longitude);
                    }

                    if (radius != null) {
                        uriBuilder.queryParam("radius", radius);
                    }

                    if (categoryGroupCode != null) {
                        uriBuilder.queryParam("category_group_code", categoryGroupCode);
                    }

                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .doOnNext(body ->
                                        log.warn("Kakao API 4xx error response: {}", body)
                                )
                                .then(Mono.error(
                                        ApplicationException.from(PlaceErrorCase.KAKAO_CLIENT_ERROR)
                                ))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> response.bodyToMono(String.class)
                                .doOnNext(body ->
                                        log.error("Kakao API 5xx error response: {}", body)
                                )
                                .then(Mono.error(
                                        ApplicationException.from(PlaceErrorCase.KAKAO_SERVER_ERROR)
                                ))
                )
                .bodyToMono(String.class)
                .block(java.time.Duration.ofSeconds(10));
    }
}