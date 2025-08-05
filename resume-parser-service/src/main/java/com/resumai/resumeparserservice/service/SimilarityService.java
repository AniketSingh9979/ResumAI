package com.resumai.resumeparserservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SimilarityService {

    /**
     * Calculate cosine similarity between resume text and job description
     * Enhanced with dynamic skill extraction and intelligent matching
     */
    public double calculateSimilarity(String resumeText, String jobDescriptionText) {
        if (resumeText == null || jobDescriptionText == null || 
            resumeText.trim().isEmpty() || jobDescriptionText.trim().isEmpty()) {
            return 0.0;
        }

        // Use enhanced text preprocessing
        String processedResume = enhancedPreprocessText(resumeText);
        String processedJobDesc = enhancedPreprocessText(jobDescriptionText);

        // Calculate multiple similarity approaches and combine them
        double basicSimilarity = calculateBasicCosineSimilarity(processedResume, processedJobDesc);
        double dynamicSkillSimilarity = calculateDynamicSkillSimilarity(processedResume, processedJobDesc);
        double keywordSimilarity = calculateKeywordSimilarity(processedResume, processedJobDesc);
        
        // Weighted combination - almost exclusively focus on dynamic skills extracted from JD
        double finalSimilarity = (basicSimilarity * 0.1) + (dynamicSkillSimilarity * 0.85) + (keywordSimilarity * 0.05);
        
        log.info("Similarity breakdown - Basic: {}, Dynamic Skills: {}, Keyword: {}, Final: {}", 
                basicSimilarity, dynamicSkillSimilarity, keywordSimilarity, finalSimilarity);
        
        return Math.round(finalSimilarity * 10000.0) / 10000.0;
    }

    /**
     * Enhanced preprocessing that preserves technical terms better
     */
    private String enhancedPreprocessText(String text) {
        return text.toLowerCase()
                   // Preserve specific technical patterns only
                   .replaceAll("\\bspring\\s+boot\\b", "springboot")
                   .replaceAll("\\brest\\s+api\\b", "restapi") 
                   .replaceAll("\\bmachine\\s+learning\\b", "machinelearning")
                   .replaceAll("\\bci/cd\\b", "cicd")
                   .replaceAll("\\bc\\+\\+", "cplusplus") // preserve C++
                   .replaceAll("\\bc#", "csharp") // preserve C#
                   .replaceAll("\\bnode\\.js\\b", "nodejs")
                   .replaceAll("\\breact\\.js\\b", "reactjs")
                   // Remove special characters but preserve alphanumeric and spaces
                   .replaceAll("[^a-zA-Z0-9\\s]", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }

    /**
     * Calculate similarity by dynamically extracting skills from job description
     * and finding matches in resume
     */
    private double calculateDynamicSkillSimilarity(String resumeText, String jobDescText) {
        // Extract potential skills/requirements from job description
        Set<String> jobRequirements = extractDynamicSkillsFromJobDescription(jobDescText);
        
        if (jobRequirements.isEmpty()) {
            log.info("No requirements extracted from job description");
            return 0.0;
        }
        
        // Count how many job requirements are found in resume
        Set<String> foundRequirements = new HashSet<>();
        Set<String> notFoundRequirements = new HashSet<>();
        
        for (String requirement : jobRequirements) {
            if (isRequirementInResume(requirement, resumeText)) {
                foundRequirements.add(requirement);
            } else {
                notFoundRequirements.add(requirement);
            }
        }
        
        double similarity = (double) foundRequirements.size() / jobRequirements.size();
        
        log.info("Dynamic skills matching:");
        log.info("  - Total job requirements: {}", jobRequirements.size());
        log.info("  - Found in resume: {} {}", foundRequirements.size(), foundRequirements);
        log.info("  - NOT found in resume: {} {}", notFoundRequirements.size(), notFoundRequirements);
        log.info("  - Similarity score: {}", similarity);
        
        return similarity;
    }

    /**
     * Dynamically extract skills and requirements from job description
     * This analyzes the text to find potential technical skills, tools, and requirements
     */
    private Set<String> extractDynamicSkillsFromJobDescription(String jobDescText) {
        Set<String> requirements = new HashSet<>();
        String lowerJobText = jobDescText.toLowerCase();
        
        log.info("=== EXTRACTING SKILLS FROM JOB DESCRIPTION ===");
        log.info("Job description length: {}", jobDescText.length());
        
        // 1. Extract explicit technical terms
        requirements.addAll(extractExplicitTechnicalTerms(jobDescText));
        
        // 2. Extract multi-word technical terms and phrases
        requirements.addAll(extractTechnicalPhrases(jobDescText));
        
        // 3. Extract terms that appear after common requirement keywords
        requirements.addAll(extractRequirementTerms(jobDescText));
        
        // 4. Extract capitalized technical terms (likely frameworks, tools, etc.)
        requirements.addAll(extractCapitalizedTerms(jobDescText));
        
        // 5. Extract version-specific terms (Java 8, Node.js, etc.)
        requirements.addAll(extractVersionedTerms(jobDescText));
        
        // 6. Extract common technical abbreviations
        requirements.addAll(extractTechnicalAbbreviations(jobDescText));
        
        // 7. Extract skills from bullet points and lists
        requirements.addAll(extractBulletPointSkills(jobDescText));
        
        // Filter out common words and very short terms
        Set<String> filteredRequirements = requirements.stream()
                .filter(req -> req.length() > 1) // Allow shorter terms for abbreviations
                .filter(req -> !isCommonWord(req))
                .filter(req -> !isGenericTerm(req))
                .collect(Collectors.toSet());
        
        log.info("Raw requirements extracted: {}", requirements.size());
        log.info("Filtered requirements: {}", filteredRequirements.size());
        log.info("Final job requirements: {}", filteredRequirements);
        
        return filteredRequirements;
    }

    /**
     * Extract explicit technical terms from text
     */
    private Set<String> extractExplicitTechnicalTerms(String text) {
        Set<String> terms = new HashSet<>();
        String lowerText = text.toLowerCase();
        
        // Comprehensive technical terms that should be extracted
        String[] explicitTerms = {
            // Programming Languages
            "java", "python", "javascript", "typescript", "react", "angular", "vue", "nodejs", "node",
            
            // Java Ecosystem
            "spring", "springboot", "mvc", "security", "hibernate", "jpa", "junit", "mockito", "testng",
            "maven", "gradle", "swagger", "openapi",
            
            // Web & APIs
            "rest", "restful", "api", "apis", "json", "xml", "http", "https",
            
            // Architecture
            "microservices", "microservice", "distributed", "architecture", "patterns",
            
            // Cloud & DevOps  
            "docker", "kubernetes", "jenkins", "aws", "azure", "gcp", "ec2", "s3", "lambda", 
            "rds", "cloudformation", "terraform", "cicd", "devops",
            
            // Databases
            "mysql", "postgresql", "mongodb", "redis", "elasticsearch", "sql", "nosql",
            
            // Version Control & Tools
            "git", "github", "gitlab", "kafka", "rabbitmq", "prometheus", "grafana",
            
            // Methodologies
            "agile", "scrum", "kanban", "tdd",
            
            // Monitoring & Testing
            "newrelic", "appdynamics", "testing", "integration", "automated"
        };
        
        // Check for exact matches and variations
        for (String term : explicitTerms) {
            if (lowerText.contains(term)) {
                terms.add(term);
                log.debug("Found explicit term: {}", term);
            }
        }
        
        // Check for versioned terms like "Java 11+", "Java 8"
        java.util.regex.Pattern versionPattern = java.util.regex.Pattern.compile("\\b(java|python|node)\\s*(\\d+[+]?)\\b");
        java.util.regex.Matcher versionMatcher = versionPattern.matcher(lowerText);
        while (versionMatcher.find()) {
            String base = versionMatcher.group(1);
            String version = versionMatcher.group(2);
            terms.add(base);
            terms.add(base + version.replaceAll("[^a-zA-Z0-9]", ""));
            log.debug("Found versioned term: {} {}", base, version);
        }
        
        // Check for compound terms that were preprocessed
        if (lowerText.contains("spring boot") || lowerText.contains("springboot")) {
            terms.add("spring");
            terms.add("springboot");
            terms.add("boot");
        }
        
        return terms;
    }

    /**
     * Extract technical abbreviations and acronyms
     */
    private Set<String> extractTechnicalAbbreviations(String text) {
        Set<String> abbreviations = new HashSet<>();
        
        // Pattern for all-caps abbreviations (likely technical)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b[A-Z]{2,}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String abbrev = matcher.group().toLowerCase();
            if (abbrev.length() >= 2 && abbrev.length() <= 6) {
                abbreviations.add(abbrev);
            }
        }
        
        return abbreviations;
    }

    /**
     * Extract skills from bullet points and lists
     */
    private Set<String> extractBulletPointSkills(String text) {
        Set<String> skills = new HashSet<>();
        
        // Split by common bullet point indicators
        String[] lines = text.split("[\n\r•\\-\\*]");
        
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 5) { // Skip very short lines
                // Look for patterns like "Experience with X" or "Knowledge of Y"
                String[] words = line.toLowerCase().split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i].replaceAll("[^a-zA-Z0-9]", "");
                    if (word.length() > 2 && !isCommonWord(word) && !isGenericTerm(word)) {
                        // Check if it looks technical
                        if (isPotentialTechnicalTerm(word)) {
                            skills.add(word);
                        }
                    }
                }
            }
        }
        
        return skills;
    }

    /**
     * Check if a word is likely a technical term based on patterns
     */
    private boolean isPotentialTechnicalTerm(String word) {
        String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        
        // Check for technical patterns
        return cleanWord.length() > 2 && (
            // Programming languages pattern
            cleanWord.matches(".*[a-z]+(script|lang|sql|ql).*") ||
            // Framework/library patterns
            cleanWord.matches(".*(framework|lib|api|sdk|orm|mvc|boot|spring|react|angular).*") ||
            // Tool patterns
            cleanWord.matches(".*(tool|engine|server|client|kit|hub|lab).*") ||
            // Technology patterns
            cleanWord.matches(".*(tech|dev|ops|ci|cd|ml|ai|db|nosql).*") ||
            // Cloud/infrastructure patterns
            cleanWord.matches(".*(cloud|aws|azure|docker|kubernetes|jenkins).*") ||
            // File extensions or technical suffixes
            cleanWord.matches(".*\\.(js|py|java|cpp|rb|go|rs|php)$") ||
            // Has numbers (version numbers)
            cleanWord.matches(".*\\d+.*") ||
            // Contains common tech abbreviations
            cleanWord.matches(".*(xml|json|http|tcp|ssl|tls|jwt|oauth).*")
        );
    }

    /**
     * Extract technical phrases from job description
     */
    private Set<String> extractTechnicalPhrases(String text) {
        Set<String> phrases = new HashSet<>();
        
        // Common technical phrases patterns
        String[] patterns = {
            "\\b[A-Z][a-z]+\\s+[A-Z][a-z]+\\b", // Spring Boot, Machine Learning
            "\\b[a-zA-Z]+\\s*\\d+(\\.\\d+)?\\b", // Java 8, Python 3.9
            "\\b[A-Z]{2,}\\b", // AWS, SQL, API, REST
            "\\b[a-z]+\\.js\\b", // React.js, Node.js
            "\\b[a-z]+\\.net\\b", // .NET
            "\\b[A-Z][a-z]+[A-Z][a-z]+\\b" // CamelCase terms like JavaScript
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                String phrase = m.group().toLowerCase()
                    .replaceAll("\\s+", "")
                    .replaceAll("[^a-zA-Z0-9]", "");
                if (phrase.length() > 2) {
                    phrases.add(phrase);
                }
            }
        }
        
        return phrases;
    }

    /**
     * Extract terms that appear after requirement keywords
     */
    private Set<String> extractRequirementTerms(String text) {
        Set<String> terms = new HashSet<>();
        
        // Keywords that typically precede requirements
        String[] requirementKeywords = {
            "experience with", "knowledge of", "proficient in", "familiar with",
            "expertise in", "understanding of", "skills in", "background in",
            "working with", "using", "developing with", "building with"
        };
        
        for (String keyword : requirementKeywords) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                keyword + "\\s+([a-zA-Z0-9\\s,/\\-\\.]+?)(?:\\.|,|;|\\band\\b|\\bor\\b|$)",
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher matcher = pattern.matcher(text);
            
            while (matcher.find()) {
                String requirement = matcher.group(1);
                // Split by common separators and extract individual terms
                String[] parts = requirement.split("[,/\\-]|\\sand\\b|\\bor\\b");
                for (String part : parts) {
                    String cleaned = part.trim().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
                    if (cleaned.length() > 2 && !isCommonWord(cleaned)) {
                        terms.add(cleaned.replaceAll("\\s+", ""));
                    }
                }
            }
        }
        
        return terms;
    }

    /**
     * Extract capitalized terms that are likely technical names
     */
    private Set<String> extractCapitalizedTerms(String text) {
        Set<String> terms = new HashSet<>();
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b[A-Z][a-zA-Z0-9]*\\b");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String term = matcher.group().toLowerCase();
            if (term.length() > 2 && !isCommonWord(term) && !isGenericTerm(term)) {
                terms.add(term);
            }
        }
        
        return terms;
    }

    /**
     * Extract versioned terms (Java 8, Python 3.x, etc.)
     */
    private Set<String> extractVersionedTerms(String text) {
        Set<String> terms = new HashSet<>();
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "\\b([a-zA-Z]+)\\s*(\\d+(?:\\.\\d+)?)\\b",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String tech = matcher.group(1).toLowerCase();
            String version = matcher.group(2);
            
            if (tech.length() > 2 && !isCommonWord(tech)) {
                terms.add(tech); // Add base technology
                terms.add(tech + version); // Add versioned term
            }
        }
        
        return terms;
    }

    /**
     * Check if a requirement from job description exists in resume
     */
    private boolean isRequirementInResume(String requirement, String resumeText) {
        String lowerResume = resumeText.toLowerCase();
        String lowerReq = requirement.toLowerCase();
        
        log.debug("Checking requirement '{}' in resume", requirement);
        
        // Direct match
        if (lowerResume.contains(lowerReq)) {
            log.debug("✅ Direct match found for '{}'", requirement);
            return true;
        }
        
        // Check for alternative forms (plurals, different spellings)
        String[] alternatives = generateAlternatives(lowerReq);
        for (String alt : alternatives) {
            if (lowerResume.contains(alt)) {
                log.debug("✅ Alternative match found for '{}' -> '{}'", requirement, alt);
                return true;
            }
        }
        
        // Partial matches for compound terms
        if (lowerReq.contains(" ") || lowerReq.length() > 8) {
            String[] reqParts = lowerReq.split("\\s+");
            int matches = 0;
            for (String part : reqParts) {
                if (part.length() > 2 && lowerResume.contains(part)) {
                    matches++;
                }
            }
            // Consider it a match if most parts are found
            if (matches >= reqParts.length * 0.5) { // Even more lenient threshold
                log.debug("✅ Partial match found for '{}' ({}/{} parts)", requirement, matches, reqParts.length);
                return true;
            }
        }
        
        // Fuzzy matching for similar terms
        String[] resumeWords = lowerResume.split("\\s+");
        for (String resumeWord : resumeWords) {
            resumeWord = resumeWord.replaceAll("[^a-zA-Z0-9]", "");
            if (resumeWord.length() > 2 && calculateLevenshteinSimilarity(lowerReq, resumeWord) > 0.75) { // Even more lenient
                log.debug("✅ Fuzzy match found for '{}' -> '{}'", requirement, resumeWord);
                return true;
            }
            
            // Also check if the requirement is contained within longer words
            if (resumeWord.length() > lowerReq.length() && resumeWord.contains(lowerReq)) {
                log.debug("✅ Substring match found for '{}' in '{}'", requirement, resumeWord);
                return true;
            }
        }
        
        log.debug("❌ No match found for '{}'", requirement);
        return false;
    }

    /**
     * Generate alternative forms of a requirement for better matching
     */
    private String[] generateAlternatives(String requirement) {
        Set<String> alternatives = new HashSet<>();
        
        // Add the original
        alternatives.add(requirement);
        
        // Add plural/singular forms
        if (requirement.endsWith("s") && requirement.length() > 3) {
            alternatives.add(requirement.substring(0, requirement.length() - 1));
        } else {
            alternatives.add(requirement + "s");
        }
        
        // Add comprehensive technical variations
        if (requirement.equals("javascript")) {
            alternatives.add("js");
        } else if (requirement.equals("js")) {
            alternatives.add("javascript");
        }
        
        if (requirement.equals("springboot")) {
            alternatives.add("spring boot");
            alternatives.add("spring");
            alternatives.add("boot");
        } else if (requirement.equals("spring")) {
            alternatives.add("springboot");
            alternatives.add("spring boot");
        }
        
        if (requirement.equals("restful")) {
            alternatives.add("rest");
            alternatives.add("rest api");
            alternatives.add("api");
        } else if (requirement.equals("rest")) {
            alternatives.add("restful");
            alternatives.add("rest api");
        }
        
        if (requirement.equals("microservices")) {
            alternatives.add("microservice");
            alternatives.add("micro services");
            alternatives.add("microservice architecture");
        } else if (requirement.equals("microservice")) {
            alternatives.add("microservices");
        }
        
        if (requirement.equals("kubernetes")) {
            alternatives.add("k8s");
        } else if (requirement.equals("k8s")) {
            alternatives.add("kubernetes");
        }
        
        if (requirement.equals("cicd")) {
            alternatives.add("ci/cd");
            alternatives.add("ci cd");
            alternatives.add("continuous integration");
            alternatives.add("continuous deployment");
        }
        
        if (requirement.equals("postgresql")) {
            alternatives.add("postgres");
        } else if (requirement.equals("postgres")) {
            alternatives.add("postgresql");
        }
        
        if (requirement.equals("automated")) {
            alternatives.add("automation");
            alternatives.add("testing");
        }
        
        if (requirement.equals("devops")) {
            alternatives.add("dev ops");
            alternatives.add("deployment");
        }
        
        return alternatives.toArray(new String[0]);
    }

    /**
     * Check if a term is too generic to be useful
     */
    private boolean isGenericTerm(String term) {
        Set<String> genericTerms = Set.of(
            "development", "programming", "software", "application", "system", "technology",
            "solution", "platform", "framework", "language", "database", "server", "client",
            "tools", "years", "experience", "skills", "knowledge", "understanding", "ability",
            "required", "preferred", "must", "should", "strong", "excellent", "good", "solid"
        );
        return genericTerms.contains(term.toLowerCase());
    }

    /**
     * Calculate keyword-based similarity for important terms
     */
    private double calculateKeywordSimilarity(String resumeText, String jobText) {
        List<String> resumeWords = Arrays.asList(resumeText.split("\\s+"));
        List<String> jobWords = Arrays.asList(jobText.split("\\s+"));
        
        Set<String> resumeSet = resumeWords.stream()
            .filter(word -> word.length() > 3)
            .filter(word -> !isCommonWord(word))
            .collect(Collectors.toSet());
            
        Set<String> jobSet = jobWords.stream()
            .filter(word -> word.length() > 3)
            .filter(word -> !isCommonWord(word))
            .collect(Collectors.toSet());
        
        Set<String> intersection = new HashSet<>(resumeSet);
        intersection.retainAll(jobSet);
        
        Set<String> union = new HashSet<>(resumeSet);
        union.addAll(jobSet);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * Original cosine similarity calculation (improved)
     */
    private double calculateBasicCosineSimilarity(String resumeText, String jobText) {
        // Create TF-IDF vectors with improved corpus
        List<String> corpus = Arrays.asList(resumeText, jobText);
        Map<String, Double> resumeVector = createTfIdfVector(resumeText, corpus);
        Map<String, Double> jobVector = createTfIdfVector(jobText, corpus);

        return cosineSimilarity(resumeVector, jobVector);
    }

    /**
     * Calculate weighted similarity considering different aspects
     */
    public double calculateWeightedSimilarity(String resumeText, String jobTitle, 
                                            String jobDescription, String requirements, String responsibilities) {
        if (resumeText == null || resumeText.trim().isEmpty()) {
            return 0.0;
        }

        // Adjusted weights to emphasize requirements and technical skills
        double titleWeight = 0.15;
        double descriptionWeight = 0.25;
        double requirementsWeight = 0.40; // Increased weight for requirements
        double responsibilitiesWeight = 0.20;

        double totalScore = 0.0;
        double totalWeight = 0.0;

        // Calculate similarity for each component
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            double titleSimilarity = calculateSimilarity(resumeText, jobTitle);
            totalScore += titleSimilarity * titleWeight;
            totalWeight += titleWeight;
            log.debug("Title similarity: {} * {} = {}", titleSimilarity, titleWeight, titleSimilarity * titleWeight);
        }

        if (jobDescription != null && !jobDescription.trim().isEmpty()) {
            double descSimilarity = calculateSimilarity(resumeText, jobDescription);
            totalScore += descSimilarity * descriptionWeight;
            totalWeight += descriptionWeight;
            log.debug("Description similarity: {} * {} = {}", descSimilarity, descriptionWeight, descSimilarity * descriptionWeight);
        }

        if (requirements != null && !requirements.trim().isEmpty()) {
            double reqSimilarity = calculateSimilarity(resumeText, requirements);
            totalScore += reqSimilarity * requirementsWeight;
            totalWeight += requirementsWeight;
            log.debug("Requirements similarity: {} * {} = {}", reqSimilarity, requirementsWeight, reqSimilarity * requirementsWeight);
        }

        if (responsibilities != null && !responsibilities.trim().isEmpty()) {
            double respSimilarity = calculateSimilarity(resumeText, responsibilities);
            totalScore += respSimilarity * responsibilitiesWeight;
            totalWeight += responsibilitiesWeight;
            log.debug("Responsibilities similarity: {} * {} = {}", respSimilarity, responsibilitiesWeight, respSimilarity * responsibilitiesWeight);
        }

        double finalScore = totalWeight > 0 ? totalScore / totalWeight : 0.0;
        log.info("Weighted similarity final score: {} (total: {}, weight: {})", finalScore, totalScore, totalWeight);
        
        return finalScore;
    }

    /**
     * Preprocess text by converting to lowercase, removing special characters, and tokenizing
     */
    private String preprocessText(String text) {
        return text.toLowerCase()
                   .replaceAll("[^a-zA-Z0-9\\s]", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }

    /**
     * Create TF-IDF vector for a document
     */
    private Map<String, Double> createTfIdfVector(String document, List<String> corpus) {
        Map<String, Double> vector = new HashMap<>();
        
        // Tokenize document
        List<String> tokens = Arrays.asList(document.split("\\s+"));
        Set<String> uniqueTokens = new HashSet<>(tokens);

        for (String token : uniqueTokens) {
            if (token.length() > 2) { // Ignore very short words
                double tf = calculateTermFrequency(token, tokens);
                double idf = calculateInverseDocumentFrequency(token, corpus);
                double weight = tf * idf;
                
                vector.put(token, weight);
            }
        }

        return vector;
    }

    /**
     * Calculate term frequency (TF)
     */
    private double calculateTermFrequency(String term, List<String> tokens) {
        long count = tokens.stream().filter(token -> token.equals(term)).count();
        return (double) count / tokens.size();
    }

    /**
     * Calculate inverse document frequency (IDF)
     */
    private double calculateInverseDocumentFrequency(String term, List<String> corpus) {
        long documentsContainingTerm = corpus.stream()
                .filter(doc -> doc.contains(term))
                .count();
        
        if (documentsContainingTerm == 0) {
            return 0.0;
        }
        
        // Add smoothing to prevent division by zero and extreme values
        return Math.log((double) (corpus.size() + 1) / (documentsContainingTerm + 1)) + 1.0;
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double cosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        Set<String> intersection = new HashSet<>(vectorA.keySet());
        intersection.retainAll(vectorB.keySet());

        double dotProduct = intersection.stream()
                .mapToDouble(token -> vectorA.get(token) * vectorB.get(token))
                .sum();

        double normA = Math.sqrt(vectorA.values().stream()
                .mapToDouble(val -> val * val)
                .sum());

        double normB = Math.sqrt(vectorB.values().stream()
                .mapToDouble(val -> val * val)
                .sum());

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (normA * normB);
    }

    /**
     * Extract key skills from text using intelligent keyword extraction
     * Dynamic version that adapts to any domain
     */
    public Set<String> extractSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new HashSet<>();
        }

        Set<String> extractedSkills = new HashSet<>();
        
        // Only use the most reliable extraction methods for resume parsing
        extractedSkills.addAll(extractExplicitTechnicalTerms(text));
        extractedSkills.addAll(extractProgrammingLanguages(text));
        extractedSkills.addAll(extractTechnicalAbbreviations(text));
        
        // Remove very short terms and common words
        Set<String> cleanedSkills = extractedSkills.stream()
                .filter(skill -> skill.length() > 1) // Allow 2-letter abbreviations
                .filter(skill -> !isCommonWord(skill))
                .filter(skill -> !isGenericTerm(skill))
                .filter(skill -> isValidTechnicalTerm(skill))
                .collect(Collectors.toSet());
        
        log.info("Extracted {} skills from text: {}", cleanedSkills.size(), cleanedSkills);
        return cleanedSkills;
    }

    /**
     * Check if a term is a valid technical term (not garbage)
     */
    private boolean isValidTechnicalTerm(String term) {
        // Filter out obviously broken terms
        return !term.matches(".*\\d{2,}.*") && // No long numbers
               !term.matches(".*[a-z][A-Z].*") && // No mixed case (indicates joined words)
               !term.contains("html") && // Filter out html fragments
               !term.contains("file") && // Filter out file paths
               !term.matches(".*\\w{15,}.*"); // No extremely long concatenated words
    }

    /**
     * Improved direct skill matching using dynamic extraction
     */
    public double calculateDirectSkillMatch(String resumeText, String jobDescriptionText) {
        if (resumeText == null || jobDescriptionText == null || 
            resumeText.trim().isEmpty() || jobDescriptionText.trim().isEmpty()) {
            return 0.0;
        }

        // Extract skills dynamically from job description
        Set<String> jobRequiredSkills = extractDynamicSkillsFromJobDescription(jobDescriptionText);
        
        if (jobRequiredSkills.isEmpty()) {
            return 0.0;
        }

        // Count how many job requirements are found in resume
        long matchingCount = jobRequiredSkills.stream()
                .filter(skill -> isRequirementInResume(skill, resumeText))
                .count();

        double finalScore = (double) matchingCount / jobRequiredSkills.size();
        
        log.info("Direct skill matching - Job skills: {}, Found in resume: {}, Score: {}", 
                jobRequiredSkills.size(), matchingCount, finalScore);
        log.debug("Job skills extracted: {}", jobRequiredSkills);
        
        return finalScore;
    }

    /**
     * Calculate Levenshtein similarity between two strings
     */
    private double calculateLevenshteinSimilarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        
        int distance = calculateLevenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLength;
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        dp[i - 1][j] + 1,
                        Math.min(
                            dp[i][j - 1] + 1,
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                        )
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    // Helper methods for skill extraction (keeping existing methods for backward compatibility)

    /**
     * Extract technical terms and concepts from text
     */
    private Set<String> extractTechnicalTerms(String text) {
        Set<String> technicalTerms = new HashSet<>();
        
        // Look for terms that often indicate technical skills
        String[] patterns = {
            "\\b\\w*(?:development|programming|coding|engineering|architecture|design)\\w*\\b",
            "\\b\\w*(?:framework|library|platform|tool|system|database|server)\\w*\\b",
            "\\b\\w*(?:api|sdk|ide|orm|mvc|mvp|mvvm)\\w*\\b",
            "\\b\\w*(?:testing|debugging|deployment|integration|automation)\\w*\\b"
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                String term = m.group().toLowerCase().trim();
                if (!isGenericTerm(term)) {
                    technicalTerms.add(term);
                }
            }
        }
        
        return technicalTerms;
    }

    /**
     * Extract programming languages and technologies
     */
    private Set<String> extractProgrammingLanguages(String text) {
        Set<String> languages = new HashSet<>();
        
        // Common programming language patterns
        String[] langPatterns = {
            "\\bjava\\b", "\\bpython\\b", "\\bjavascript\\b", "\\btypescript\\b", "\\bc\\+\\+\\b", "\\bc#\\b",
            "\\bruby\\b", "\\bphp\\b", "\\bgo\\b", "\\brust\\b", "\\bkotlin\\b", "\\bscala\\b", "\\bswift\\b",
            "\\bhtml\\b", "\\bcss\\b", "\\bsql\\b", "\\br\\b", "\\bmatlab\\b", "\\bperl\\b", "\\bshell\\b"
        };
        
        for (String pattern : langPatterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                languages.add(m.group().toLowerCase().trim());
            }
        }
        
        return languages;
    }

    /**
     * Extract frameworks, tools, and technologies
     */
    private Set<String> extractFrameworksAndTools(String text) {
        Set<String> frameworks = new HashSet<>();
        
        // Extract capitalized terms that might be frameworks/tools
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b[A-Z][a-zA-Z0-9]*(?:\\.[a-zA-Z0-9]+)*\\b");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String term = matcher.group().toLowerCase();
            if (term.length() > 2 && !isCommonWord(term) && !isGenericTerm(term)) {
                frameworks.add(term);
            }
        }
        
        // Also look for terms with common tech suffixes
        String[] techPatterns = {
            "\\b\\w+(?:\\.js|\\.py|\\.rb|\\.go|\\.rs)\\b",
            "\\b\\w*(?:stack|tech|cloud|ops|dev)\\w*\\b"
        };
        
        for (String techPattern : techPatterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(techPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                String term = m.group().toLowerCase().trim();
                if (!isGenericTerm(term)) {
                    frameworks.add(term);
                }
            }
        }
        
        return frameworks;
    }

    /**
     * Extract soft skills and methodologies
     */
    private Set<String> extractSoftSkills(String text) {
        Set<String> softSkills = new HashSet<>();
        
        String[] skillPatterns = {
            "\\b(?:agile|scrum|kanban|waterfall|devops|ci/cd)\\b",
            "\\b(?:leadership|communication|teamwork|collaboration|problem.solving)\\b",
            "\\b(?:analytical|creative|innovative|strategic|detail.oriented)\\b",
            "\\b(?:project.management|time.management|risk.management)\\b"
        };
        
        for (String pattern : skillPatterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                softSkills.add(m.group().toLowerCase().trim().replaceAll("\\.", " "));
            }
        }
        
        return softSkills;
    }

    /**
     * Check if a word is a common word that shouldn't be considered a skill
     */
    private boolean isCommonWord(String word) {
        Set<String> commonWords = Set.of(
            "the", "and", "for", "are", "but", "not", "you", "all", "can", "had", "her", "was", "one", "our", "out", "day", "get", "has", "him", "his", "how", "man", "new", "now", "old", "see", "two", "way", "who", "boy", "did", "its", "let", "put", "say", "she", "too", "use",
            "work", "experience", "years", "responsibilities", "requirements", "skills", "knowledge", "ability", "opportunity", "position", "role", "job", "company", "team", "business", "will", "must", "should", "good", "excellent", "strong", "proven", "successful"
        );
        return commonWords.contains(word.toLowerCase());
    }
} 