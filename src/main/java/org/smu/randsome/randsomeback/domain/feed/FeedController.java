package org.smu.randsome.randsomeback.domain.feed;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class FeedController extends FeedControllerDocs {

    private final FeedService feedService;

    @GetMapping("/v1/feed")
    public ResponseEntity<ApiResponse<List<FeedItem>>> getLatestFeed(
            @RequestParam(required = false) Long lastId
    ) {
        List<FeedItem> response = feedService.getLatestFeed(lastId)
                .stream()
                .map(FeedItem::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
