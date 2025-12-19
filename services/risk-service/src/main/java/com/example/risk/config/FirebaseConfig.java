package com.example.risk.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseApp firebaseApp(FirebaseProperties properties) throws IOException {
        FirebaseOptions.Builder builder = FirebaseOptions.builder();

        if (properties.getCredentials() != null && !properties.getCredentials().isBlank()) {
            try (InputStream serviceAccount = new FileInputStream(properties.getCredentials())) {
                builder.setCredentials(GoogleCredentials.fromStream(serviceAccount));
            }
        } else {
            log.info("Using default Google credentials for Firebase Admin SDK");
            builder.setCredentials(GoogleCredentials.getApplicationDefault());
        }

        if (properties.getProjectId() != null && !properties.getProjectId().isBlank()) {
            builder.setProjectId(properties.getProjectId());
        }

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(builder.build());
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
