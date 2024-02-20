package com.pollyproject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;

@Configuration
public class AppConfig {

    @Bean
    public SynthesizeSpeechRequest synthesizeSpeechRequest() {
        return new SynthesizeSpeechRequest();
    }

}
