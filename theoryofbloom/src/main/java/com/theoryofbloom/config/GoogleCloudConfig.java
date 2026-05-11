package com.theoryofbloom.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GoogleCloudConfig {

    @Bean
    public Storage storage() throws IOException {

        GoogleCredentials credentials =
                GoogleCredentials.fromStream(
                        getClass()
                                .getClassLoader()
                                .getResourceAsStream("gcp/gcp-key.json")
                );

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
