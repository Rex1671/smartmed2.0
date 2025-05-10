package com.example.smartmedbeta;

import java.util.concurrent.*;
import java.util.*;
import org.json.JSONObject;

public class DiagnosisProcessor {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        String prompt = "Patient reports fever, headache, and nausea. Provide a possible diagnosis.";
        runParallelAIRequests(prompt);
    }

    public static void runParallelAIRequests(String prompt) {
        List<Callable<DiagnosisResult>> tasks = Arrays.asList(
                () -> queryGeminiAI(prompt),
                () -> queryHuggingFaceAI(prompt),
                () -> queryMistralAI(prompt),
                () -> queryLlama2AI(prompt)
        );

        try {
            List<Future<DiagnosisResult>> results = executor.invokeAll(tasks);
            List<DiagnosisResult> responses = new ArrayList<>();

            for (Future<DiagnosisResult> future : results) {
                responses.add(future.get());
            }

            DiagnosisResult bestDiagnosis = selectBestDiagnosis(responses);
            System.out.println("Best Diagnosis: " + bestDiagnosis);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private static DiagnosisResult selectBestDiagnosis(List<DiagnosisResult> responses) {
        Map<String, Integer> diagnosisCount = new HashMap<>();
        Map<String, Double> confidenceScores = new HashMap<>();

        for (DiagnosisResult result : responses) {
            diagnosisCount.put(result.condition, diagnosisCount.getOrDefault(result.condition, 0) + 1);
            confidenceScores.put(result.condition, confidenceScores.getOrDefault(result.condition, 0.0) + result.confidence);
        }

        String bestCondition = Collections.max(diagnosisCount.entrySet(), Map.Entry.comparingByValue()).getKey();
        double avgConfidence = confidenceScores.get(bestCondition) / diagnosisCount.get(bestCondition);

        return new DiagnosisResult(bestCondition, avgConfidence);
    }

    private static DiagnosisResult queryGeminiAI(String prompt) {
        return new DiagnosisResult("Flu", 0.85);
    }

    private static DiagnosisResult queryHuggingFaceAI(String prompt) {
        return new DiagnosisResult("Common Cold", 0.78);
    }

    private static DiagnosisResult queryMistralAI(String prompt) {
        return new DiagnosisResult("Flu", 0.82);
    }

    private static DiagnosisResult queryLlama2AI(String prompt) {
        return new DiagnosisResult("Flu", 0.80);
    }
}

class DiagnosisResult {
    String condition;
    double confidence;

    public DiagnosisResult(String condition, double confidence) {
        this.condition = condition;
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "Condition: " + condition + ", Confidence: " + confidence;
    }
}
