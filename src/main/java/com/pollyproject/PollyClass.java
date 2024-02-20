package com.pollyproject;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.VoiceId;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.*;
import java.util.Scanner;

@SpringBootApplication
@ComponentScan(basePackages = "com.pollyproject")

public class PollyClass implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PollyClass.class, args);
    }

    @Autowired
    SynthesizeSpeechRequest synthesizeSpeechRequest;

    @Override
    public void run(String... arg0) throws Exception {
        // Method called when the application starts
        readFileForSynthesize();
    }

    // Method to synthesize speech from a string and play it
    public void synthesizeFromStringAndPlay(String text, AmazonPolly amazonPolly) throws IOException, JavaLayerException {
        SynthesizeSpeechRequest synthReq = synthesizeSpeechRequest.withText(text).withVoiceId(VoiceId.Aditi)
                .withOutputFormat(OutputFormat.Mp3);
        SynthesizeSpeechResult synthRes = amazonPolly.synthesizeSpeech(synthReq);
        InputStream speechStream = synthRes.getAudioStream();
        AdvancedPlayer player = new AdvancedPlayer(speechStream,
                javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());

        player.setPlayBackListener(new PlaybackListener() {
            public void playbackStarted(PlaybackEvent evt) {
                System.out.println("Playback started");
            }

            @Override
            public void playbackFinished(PlaybackEvent evt) {
                System.out.println("Playback finished");
            }
        });

        // Play the synthesized speech
        player.play();
    }

    // Method to synthesize speech from a string and save it to a file
    public void synthesizeAndSaveToFile(String text, String outputFileName, AmazonPolly amazonPolly) {
        SynthesizeSpeechRequest request = synthesizeSpeechRequest.withOutputFormat(OutputFormat.Mp3)
                .withVoiceId(VoiceId.Raveena).withText(text);
        try (FileOutputStream outputStream = new FileOutputStream(new File(outputFileName))) {
            SynthesizeSpeechResult synthesizeSpeechResult = amazonPolly.synthesizeSpeech(request);
            byte[] buffer = new byte[2 * 1024];
            int readBytes;

            // Write the synthesized speech to the output file
            try (InputStream in = synthesizeSpeechResult.getAudioStream()) {
                while ((readBytes = in.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readBytes);
                }
            }
        } catch (Exception e) {
            System.err.println("Exception caught: " + e);
        }
    }

    // Method to read text data from a file, synthesize speech, and play it or save it to a file
    public void readFileForSynthesize() throws JavaLayerException {
        String line = "";
        StringBuilder builder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File("C:\\Users\\Saood\\Desktop\\Spring Java\\Aws-polly\\Testdata1.txt"))))) {
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            System.out.println(builder.toString());

            // Initialize AWS credentials
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter AWS Access Key: ");
            String accessKey = scanner.nextLine();

            System.out.print("Enter AWS Secret Key: ");
            String secretKey = scanner.nextLine();
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

            // Initialize Amazon Polly client
            AmazonPolly amazonPolly = AmazonPollyClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1) // Specify the region
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();

            // Synthesize speech from the text and play it or save it to a file
            synthesizeFromStringAndPlay(builder.toString(), amazonPolly);
            synthesizeAndSaveToFile(builder.toString(), "C:\\Users\\Saood\\Desktop\\Spring Java\\Aws-polly\\amazon-speech.mp3", amazonPolly);
        } catch (IOException ex) {
            System.err.println("Exception caught: " + ex);
        }
    }
}
