package org.smu.randsome.randsomeback.domain.matching.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.service.MatchingService;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MatchingController extends MatchingControllerDocs {

    private final MatchingService matchingService;

}