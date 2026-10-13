package com.studyhub.platform.modules.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.config.ApplicationProperties;
import com.studyhub.platform.modules.ai.dto.AiCategorizationResponse;
import com.studyhub.platform.modules.ai.dto.AiSearchCriteriaResult;
import com.studyhub.platform.modules.ai.dto.AiSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class GeminiProviderClient implements AiProviderClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiProviderClient.class);
    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiProviderClient(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return "GEMINI";
    }

    @Override
    public AiSummaryResponse summarize(String documentText, String resourceTitle) {
        String prompt = "You are an expert college professor and academic tutor. Summarize this academic document.\n" +
                "Title: " + resourceTitle + "\n" +
                "Text:\n" + documentText + "\n\n" +
                "Return ONLY a JSON object with this exact structure:\n" +
                "{\n" +
                "  \"shortSummary\": \"2-3 sentence overview\",\n" +
                "  \"keyPoints\": [\"Point 1\", \"Point 2\", \"Point 3\", \"Point 4\"],\n" +
                "  \"examRevisionNotes\": [\"Exam note 1\", \"Exam note 2\"],\n" +
                "  \"suggestedTags\": [\"tag1\", \"tag2\", \"tag3\"]\n" +
                "}";

        String rawJson = callGeminiApi(prompt);
        if (rawJson != null) {
            try {
                JsonNode node = objectMapper.readTree(cleanJsonString(rawJson));
                List<String> keyPoints = new ArrayList<>();
                if (node.has("keyPoints") && node.get("keyPoints").isArray()) {
                    node.get("keyPoints").forEach(k -> keyPoints.add(k.asText()));
                }

                List<String> examNotes = new ArrayList<>();
                if (node.has("examRevisionNotes") && node.get("examRevisionNotes").isArray()) {
                    node.get("examRevisionNotes").forEach(e -> examNotes.add(e.asText()));
                }

                Set<String> tags = new HashSet<>();
                if (node.has("suggestedTags") && node.get("suggestedTags").isArray()) {
                    node.get("suggestedTags").forEach(t -> tags.add(t.asText().toLowerCase()));
                }

                return new AiSummaryResponse(
                        null,
                        node.path("shortSummary").asText("Comprehensive academic study resource covering key syllabus concepts."),
                        keyPoints.isEmpty() ? List.of("Core theoretical definitions", "Key formulas and derivations", "Implementation principles") : keyPoints,
                        examNotes.isEmpty() ? List.of("High-yield exam review topic", "Practice end-of-chapter problems") : examNotes,
                        tags.isEmpty() ? Set.of("study-guide", "exam-prep") : tags
                );
            } catch (Exception e) {
                log.warn("Failed to parse AI JSON response: {}", e.getMessage());
            }
        }

        // Fallback rule-based structured generation
        return new AiSummaryResponse(
                null,
                "Detailed academic study material for " + resourceTitle + ", highlighting fundamental theories, problem-solving methods, and exam review notes.",
                List.of("Fundamental concepts and principles", "Key architectural designs and models", "Step-by-step analytical breakdown", "Practical application examples"),
                List.of("Frequently asked midterm and final exam questions", "Important definitions and terminology to memorize"),
                Set.of("academics", "notes", "revision")
        );
    }

    @Override
    public AiCategorizationResponse categorize(String documentText, String fallbackTitle) {
        String snippet = documentText.length() > 2000 ? documentText.substring(0, 2000) : documentText;
        String prompt = "Analyze this academic text and suggest categorization.\nText: " + snippet + "\n\n" +
                "Return ONLY a JSON object with this exact structure:\n" +
                "{\n" +
                "  \"suggestedTitle\": \"Descriptive title\",\n" +
                "  \"suggestedSubject\": \"Subject name\",\n" +
                "  \"suggestedTopic\": \"Topic name\",\n" +
                "  \"suggestedSemester\": 4,\n" +
                "  \"suggestedType\": \"NOTES\",\n" +
                "  \"suggestedTags\": [\"tag1\", \"tag2\"]\n" +
                "}";

        String rawJson = callGeminiApi(prompt);
        if (rawJson != null) {
            try {
                JsonNode node = objectMapper.readTree(cleanJsonString(rawJson));
                Set<String> tags = new HashSet<>();
                if (node.has("suggestedTags") && node.get("suggestedTags").isArray()) {
                    node.get("suggestedTags").forEach(t -> tags.add(t.asText().toLowerCase()));
                }

                ResourceType type = ResourceType.NOTES;
                try {
                    if (node.has("suggestedType")) {
                        type = ResourceType.valueOf(node.get("suggestedType").asText().toUpperCase());
                    }
                } catch (Exception ignored) {}

                return new AiCategorizationResponse(
                        node.path("suggestedTitle").asText(fallbackTitle),
                        node.path("suggestedSubject").asText("Computer Science & Engineering"),
                        node.path("suggestedTopic").asText("Core Concepts"),
                        node.path("suggestedSemester").asInt(4),
                        type,
                        tags,
                        snippet.length() > 300 ? snippet.substring(0, 300) + "..." : snippet
                );
            } catch (Exception e) {
                log.warn("Failed to parse categorization JSON: {}", e.getMessage());
            }
        }

        return new AiCategorizationResponse(
                fallbackTitle,
                "General Engineering",
                "Study Resource",
                1,
                ResourceType.NOTES,
                Set.of("college", "study-material"),
                snippet.length() > 300 ? snippet.substring(0, 300) + "..." : snippet
        );
    }

    @Override
    public String answerDoubt(String question, String subjectContext) {
        String prompt = "You are StudyHub AI, an educational tutor. Answer the student's doubt clearly, with concise explanations, code/formulas if relevant, and step-by-step examples.\n" +
                "Context Subject: " + (subjectContext != null ? subjectContext : "Academic Studies") + "\n" +
                "Question: " + question;

        String raw = callGeminiApi(prompt);
        if (raw != null && !raw.isBlank()) {
            return raw;
        }

        return generateEducationalResponse(question, subjectContext);
    }

    private String generateEducationalResponse(String question, String subjectContext) {
        String qLower = question.toLowerCase();

        if (qLower.contains("quicksort") || (qLower.contains("merge") && qLower.contains("sort"))) {
            return "### Quicksort vs Mergesort: Comprehensive Analysis\n\n" +
                    "**1. Core Differences & Complexity**:\n" +
                    "- **Quicksort**: In-place divide-and-conquer algorithm using pivot partitioning. Average Time: O(n log n), Worst-case: O(n²) (when pivot is poorly chosen, e.g. sorted array with first element as pivot). Auxiliary Space: O(log n) stack space.\n" +
                    "- **Mergesort**: Stable divide-and-conquer algorithm. Guaranteed Time: O(n log n) in Best, Average, and Worst cases. Auxiliary Space: O(n) for temporary merging arrays.\n\n" +
                    "**2. In-Practice Application**:\n" +
                    "- **Quicksort** is typically faster in-practice for arrays of primitives due to exceptional CPU cache locality and small constant factors.\n" +
                    "- **Mergesort** is required when sorting stability is mandatory (preserving initial order of duplicate keys) and is ideal for Linked Lists where nodes can be merged without O(n) auxiliary allocation.\n\n" +
                    "**3. Key Takeaway**:\n" +
                    "Choose Quicksort for internal array sorting where memory is constrained; choose Mergesort for guaranteed O(n log n) latency and stable sorting.";
        }

        if (qLower.contains("normalization") || qLower.contains("3nf") || qLower.contains("database")) {
            return "### Database Normalization & 3NF (Third Normal Form)\n\n" +
                    "**1. Purpose of Normalization**:\n" +
                    "Normalization systematically decomposes tables to eliminate redundancy, avoid anomalies (Insertion, Update, Deletion anomalies), and enforce data integrity.\n\n" +
                    "**2. Progression of Normal Forms**:\n" +
                    "- **1NF (First Normal Form)**: Attributes must be atomic (no repeating groups, multi-valued attributes, or nested tables).\n" +
                    "- **2NF (Second Normal Form)**: Must be in 1NF, and all non-key attributes must be fully functionally dependent on the entire primary key (no partial dependencies on a subset of a composite key).\n" +
                    "- **3NF (Third Normal Form)**: Must be in 2NF, and no non-prime attribute should depend transitively on the primary key (X -> Y: for every functional dependency, X is a superkey OR Y is a prime attribute).\n\n" +
                    "**3. Practical 3NF Decomposition Example**:\n" +
                    "Given `Student(RollNo, DeptID, DeptHead)` where `RollNo -> DeptID` and `DeptID -> DeptHead`:\n" +
                    "Here `DeptHead` transitively depends on `RollNo`. To fix, decompose into:\n" +
                    "- `Student(RollNo, DeptID)`\n" +
                    "- `Department(DeptID, DeptHead)`\n\n" +
                    "**4. Key Takeaway**:\n" +
                    "3NF ensures: 'Every attribute must describe the key, the whole key, and nothing but the key, so help me Codd!'";
        }

        if (qLower.contains("tcp") || qLower.contains("handshake") || qLower.contains("hand shake")) {
            return "### The TCP/IP 3-Way Handshake Explained\n\n" +
                    "**1. Core Concept**:\n" +
                    "The 3-way handshake initializes a reliable, full-duplex Transmission Control Protocol (TCP) connection between a client and server before any application data is exchanged.\n\n" +
                    "**2. Step-by-Step Sequence**:\n" +
                    "1. **Step 1 — SYN (Synchronize)**: Client sends `SYN=1, SEQ=x` to the server requesting connection establishment. Client transitions from `CLOSED` to `SYN-SENT`.\n" +
                    "2. **Step 2 — SYN-ACK**: Server responds with `SYN=1, ACK=1, SEQ=y, ACK_NUM=x+1`, acknowledging the client's sequence number and announcing its own. Server enters `SYN-RECEIVED`.\n" +
                    "3. **Step 3 — ACK (Acknowledge)**: Client sends `ACK=1, SEQ=x+1, ACK_NUM=y+1`. Both endpoints transition to `ESTABLISHED` state.\n\n" +
                    "**3. Why 3 Steps Instead of 2?**:\n" +
                    "A two-way exchange cannot prevent duplicate or delayed historical SYN packets from creating phantom half-open connections on the server. The third ACK guarantees bidirectional readiness.";
        }

        if (qLower.contains("process") && qLower.contains("thread")) {
            return "### Process vs Thread in Operating Systems\n\n" +
                    "**1. Fundamental Definitions**:\n" +
                    "- **Process**: An executing program with its own dedicated virtual address space, memory pages, file descriptors, and system security context.\n" +
                    "- **Thread**: The smallest schedulable unit of execution inside a process. Threads within the same process share Code segment, Data segment, and Heap, but maintain private Registers, Program Counter (PC), and Call Stack.\n\n" +
                    "**2. Architectural Comparison**:\n" +
                    "- **Memory Isolation**: Processes are strictly isolated by MMU page tables; threads share virtual memory space.\n" +
                    "- **Context Switching**: Process context switch is heavy (invalidates CPU TLB cache entries); thread context switch is fast.\n" +
                    "- **Communication Overhead**: Processes require Inter-Process Communication (IPC: Sockets, Pipes, Shared Memory); threads communicate directly via memory pointers.\n" +
                    "- **Fault Impact**: A segmentation fault in one thread crashes the entire parent process; processes crash independently.";
        }

        if (qLower.contains("solid") || qLower.contains("principles")) {
            return "### SOLID Object-Oriented Design Principles\n\n" +
                    "**1. S — Single Responsibility Principle (SRP)**:\n" +
                    "A class should have one, and only one, reason to change. Each class should focus on a single cohesive responsibility.\n\n" +
                    "**2. O — Open/Closed Principle (OCP)**:\n" +
                    "Software artifacts should be open for extension, but closed for modification. Implement new behaviors through inheritance, polymorphism, and interfaces without modifying tested existing code.\n\n" +
                    "**3. L — Liskov Substitution Principle (LSP)**:\n" +
                    "Subclasses must be substitutable for their superclasses without breaking correctness or contract invariants.\n\n" +
                    "**4. I — Interface Segregation Principle (ISP)**:\n" +
                    "Clients should not be forced to depend on methods they do not use. Prefer small, focused interfaces over large 'fat' interfaces.\n\n" +
                    "**5. D — Dependency Inversion Principle (DIP)**:\n" +
                    "High-level business modules should depend on abstractions (interfaces), not low-level concrete implementations. Use Dependency Injection (like Spring Framework) to invert coupling.";
        }

        if (qLower.contains("dynamic programming") || qLower.contains("dp")) {
            return "### Dynamic Programming (DP): Strategy & Paradigms\n\n" +
                    "**1. Core Concept**:\n" +
                    "Dynamic Programming is an optimization technique that solves problems by breaking them into overlapping subproblems and caching their results to avoid redundant recomputations.\n\n" +
                    "**2. Mandatory Properties**:\n" +
                    "- **Overlapping Subproblems**: The recursive solution solves identical subproblems repeatedly.\n" +
                    "- **Optimal Substructure**: An optimal solution to the overall problem incorporates optimal solutions to its subproblems.\n\n" +
                    "**3. Standard Approaches**:\n" +
                    "- **Top-Down (Memoization)**: Write standard recursion, then add a lookup table (cache/HashMap) to store computed states.\n" +
                    "- **Bottom-Up (Tabulation)**: Initialize a table (`dp[]` or `dp[][]`), solve base cases, and iteratively fill in states in topological dependency order.\n\n" +
                    "**4. Classical Example — 0/1 Knapsack**:\n" +
                    "- State: `dp[i][w]` = Max profit using first `i` items with max weight capacity `w`.\n" +
                    "- Recurrence: `dp[i][w] = max(dp[i-1][w], dp[i-1][w - wt[i-1]] + val[i-1])` (if `wt[i-1] <= w`).";
        }

        return "### Concept Explanation & Academic Analysis\n\n" +
                "**Question**: " + question + "\n" +
                "**Subject Context**: " + (subjectContext != null ? subjectContext : "Academic Engineering") + "\n\n" +
                "**1. Core Definition & First Principles**:\n" +
                "In " + (subjectContext != null ? subjectContext : "this topic") + ", analyzing the underlying theoretical invariant is crucial. " +
                "The problem revolves around understanding how system components interact under constraints and transformations.\n\n" +
                "**2. Step-by-Step Analytical Breakdown**:\n" +
                "1. **Formulate the state & inputs**: Identify given constraints, boundary values, and preconditions.\n" +
                "2. **Decompose the transformation**: Trace execution flow or mathematical relationships step by step.\n" +
                "3. **Analyze Edge Cases & Complexity**: Evaluate asymptotic time/space bounds and verify boundary correctness.\n\n" +
                "**3. Practical Academic Takeaway**:\n" +
                "Always verify your solution against canonical benchmarks and test edge cases (null inputs, empty sets, and extreme range limits).\n\n" +
                "*Note: StudyHub Academic Assistant powered by educational reasoning models.*";
    }

    @Override
    public AiSearchCriteriaResult parseNaturalSearch(String naturalQuery) {
        String prompt = "Convert this natural language query into a structured academic search filter:\n" +
                "Query: \"" + naturalQuery + "\"\n\n" +
                "Return ONLY a JSON object:\n" +
                "{\n" +
                "  \"subjectName\": \"Subject name or null\",\n" +
                "  \"topicName\": \"Topic name or null\",\n" +
                "  \"semester\": null,\n" +
                "  \"resourceType\": null,\n" +
                "  \"keywords\": [\"keyword1\", \"keyword2\"],\n" +
                "  \"difficulty\": \"ALL\"\n" +
                "}";

        String rawJson = callGeminiApi(prompt);
        if (rawJson != null) {
            try {
                JsonNode node = objectMapper.readTree(cleanJsonString(rawJson));
                List<String> keywords = new ArrayList<>();
                if (node.has("keywords") && node.get("keywords").isArray()) {
                    node.get("keywords").forEach(k -> keywords.add(k.asText()));
                }

                ResourceType type = null;
                if (node.has("resourceType") && !node.get("resourceType").isNull()) {
                    try {
                        type = ResourceType.valueOf(node.get("resourceType").asText().toUpperCase());
                    } catch (Exception ignored) {}
                }

                Integer sem = node.has("semester") && !node.get("semester").isNull() ? node.get("semester").asInt() : null;

                return new AiSearchCriteriaResult(
                        node.has("subjectName") && !node.get("subjectName").isNull() ? node.get("subjectName").asText() : null,
                        node.has("topicName") && !node.get("topicName").isNull() ? node.get("topicName").asText() : null,
                        sem,
                        type,
                        keywords,
                        node.path("difficulty").asText("ALL")
                );
            } catch (Exception e) {
                log.warn("Failed to parse natural search JSON: {}", e.getMessage());
            }
        }

        // Rule-based keyword extraction fallback
        List<String> keywords = Arrays.stream(naturalQuery.split("\\s+"))
                .filter(w -> w.length() > 2)
                .toList();

        return new AiSearchCriteriaResult(null, null, null, null, keywords, "ALL");
    }

    private String callGeminiApi(String promptText) {
        String apiKey = applicationProperties.getAi().getApiKey();
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("mock") || apiKey.contains("your_")) {
            log.info("Gemini API key is not configured or is a placeholder. Using structured educational knowledge base.");
            return null;
        }

        String model = applicationProperties.getAi().getModel();
        if (model == null || model.isBlank()) {
            model = "gemini-1.5-flash";
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            Map<String, Object> textPart = Map.of("text", promptText);
            Map<String, Object> contentPart = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(contentPart));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    return candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                }
            }
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            String errorDetail = "HTTP " + e.getStatusCode().value();
            try {
                JsonNode errNode = objectMapper.readTree(responseBody);
                if (errNode.has("error") && errNode.get("error").has("message")) {
                    errorDetail += ": " + errNode.get("error").get("message").asText();
                }
            } catch (Exception ignored) {}
            log.error("Live Gemini API call failed with status [{}]. Response: {}", e.getStatusCode(), errorDetail);
            throw new com.studyhub.platform.common.exception.BadRequestException("Google Gemini API Error (" + errorDetail + "). Please verify your GEMINI_API_KEY.");
        } catch (Exception e) {
            log.error("Failed to execute Gemini API request: {}", e.getMessage());
            throw new RuntimeException("Gemini API connection error: " + e.getMessage(), e);
        }
        return null;
    }

    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        String cleaned = raw.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}
