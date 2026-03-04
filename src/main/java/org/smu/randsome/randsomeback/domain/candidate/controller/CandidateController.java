package org.smu.randsome.randsomeback.domain.candidate.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.service.CandidateService;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CandidateController extends CandidateControllerDocs {

    private final CandidateService candidateService;

}