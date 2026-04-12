package org.smu.randsome.randsomeback.domain.qr.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.qr.service.QrService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class QrController extends QrControllerDocs {

    private final QrService qrService;

    @Override
    @GetMapping(value = "/v1/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateMemberQr(@LoginMember Long memberId) {
        byte[] qrImage = qrService.generateMemberQr(memberId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

}
