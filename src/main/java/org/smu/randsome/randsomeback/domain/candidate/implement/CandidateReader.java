package org.smu.randsome.randsomeback.domain.candidate.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CandidateReader {

    private final CandidateJpaRepository candidateJpaRepository;

}