package com.springAIPractice.SpringAI;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GeminiController {

//    private OpenAiChatModel chatModel;
    private ChatClient chatClient;

    public GeminiController(OpenAiChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
//        this.chatModel = chatModel;
        System.out.println("GeminiController initialized with OpenAiChatModel");
    }



    @PostMapping("/text")
    public ResponseEntity<?> answer(@RequestBody Map<String, String> payload) {
        String message = payload.get("text");
        // Process message as needed
        System.out.println("Received message: " + message);

        try {
            String response = chatClient.prompt(message).call().content();
//            String response = chatModel.call(message);
            System.out.println(message);
            System.out.println(response);
            return ResponseEntity.ok(Map.of("message", response));
        } catch (Exception e) {
            // Log the error if needed
            return ResponseEntity.ok(Map.of("message", "The AI model is overloaded. Please try again later."));
        }


    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        // Process the image file
        System.out.println("Received image file: " + file.getOriginalFilename());
        String fileName = file.getOriginalFilename();

        byte[] fileBytes = file.getBytes();
        System.out.println("image converted into bytes");

//        Media imageMedia = Media.builder()
//                .data(fileBytes)
//                .mimeType(MimeType.valueOf(MimeTypeUtils.IMAGE_JPEG_VALUE))
//                .build();
//
//
//
//        // Create a UserMessage with the image
//        UserMessage userMessage = new UserMessage("This is a media object containing an image explain the image in 100 words." + List.of(imageMedia));
//
//        Prompt prompt = new Prompt(List.of(userMessage));
//        String response = chatClient.prompt(prompt).call().content();
//        System.out.println(response);
        String promptImage = "data:" + file.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(fileBytes);
        String response = chatClient.prompt(promptImage + " decode this image to a visual image and return what sentence is written in this image").call().content();
       System.out.println("data:" + file.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(fileBytes));

        // Save or process as needed
        return ResponseEntity.ok(Map.of("message", response) );
    }

    @PostMapping("/pdf")
    public ResponseEntity<?> uploadPDF(@RequestParam("file") MultipartFile file){
        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body("Please upload a valid PDF file.");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            // PDFTextStripper extracts the text from the document
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            System.out.println("Extracted Text: " + text);
            // Pass the extracted text to your AI model
            String response = chatClient.prompt(text + " this is the text extracted from a resume pdf. Rate this resume out of 10 and also tell 3 good points and 3 weaknesses keep the answer short(max 200 words).").call().content();
            System.out.println(response);
            return ResponseEntity.ok(Map.of("message", response));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing the PDF file.");
        }
    }


    @PostMapping("/pdfwithJD")
    public ResponseEntity<?> uploadPDFandJD(@RequestParam("file") MultipartFile file, @RequestParam("jobDescription") String message) {
        // The message is now received as a request parameter
        System.out.println("Received message: " + message);

        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body("Please upload a valid PDF file.");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            System.out.println("Extracted Text: " + text);

            String response = chatClient.prompt(text + " this is the text extracted from a resume pdf." +
                    " Rate this resume out of 100 based on the following Job Description." +
                    message +
                    " Also tell 3 good points and 3 weaknesses(if there are) and keep the answer short(max 150 words)." +
                    "Use ATS based grading system.").call().content();
            System.out.println(response);

            return ResponseEntity.ok(Map.of("message", response));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing the PDF file.");
        }
    }


    @GetMapping
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("message", "API is up"));
    }


}
