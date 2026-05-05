package org.smu.randsome.randsomeback.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credential-path:firebase.json}")
    private String credentialPath;

    @PostConstruct
    public void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            Resource resource = credentialPath.startsWith("/")
                    ? new FileSystemResource(credentialPath)
                    : new ClassPathResource(credentialPath);

            try (InputStream serviceAccount = resource.getInputStream()) {

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("[FirebaseConfig] Firebase 초기화 완료");

            } catch (IOException e) {
                log.error("[FirebaseConfig] Firebase 초기화 실패.", e);
                throw new CoreException(ErrorType.FIREBASE_INIT_ERROR);
            }
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }

}