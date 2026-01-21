package com.bready.server.place.external;

import com.bready.server.global.exception.ApplicationException;
import com.bready.server.place.exception.PlaceErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KakaoPlaceClient {

    private final WebClient webClient;

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

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
                        uriBuilder.queryParam("y", latitude).queryParam("x", longitude);
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
                        status -> status.is4xxClientError(),
                        response -> Mono.error(ApplicationException.from(PlaceErrorCase.KAKAO_CLIENT_ERROR))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> Mono.error(ApplicationException.from(PlaceErrorCase.KAKAO_SERVER_ERROR))
                )
                .bodyToMono(String.class)
                .block();
    }
}
