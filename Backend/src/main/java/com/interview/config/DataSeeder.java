package com.interview.config;

import com.interview.entity.Question;
import com.interview.entity.Question.QuestionType;
import com.interview.entity.Question.ExperienceLevel;
import com.interview.entity.Question.Difficulty;
import com.interview.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data Seeder Component to populate initial interview questions
 * Runs on application startup to ensure database has sample data
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    
    private final QuestionRepository questionRepository;

    @Autowired
    public DataSeeder(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting data seeding process...");
        
        try {
            // Check if data already exists
            long existingQuestions = questionRepository.count();
            if (existingQuestions > 0) {
                logger.info("Questions already exist in database ({}). Skipping data seeding.", existingQuestions);
                return;
            }

            // Seed questions for different domains and experience levels
            seedJavaQuestions();
            seedPythonQuestions();
            seedFrontendQuestions();
            seedSystemDesignQuestions();
            
            long totalSeeded = questionRepository.count();
            logger.info("Data seeding completed successfully. Total questions seeded: {}", totalSeeded);
            
        } catch (Exception e) {
            logger.error("Error during data seeding: {}", e.getMessage(), e);
        }
    }

    private void seedJavaQuestions() {
        String domain = "Java Development";
        
        // Coding Questions
        createQuestion(
            "Reverse a String",
            "Write a Java method to reverse a string without using built-in reverse methods.",
            QuestionType.CODING,
            domain,
            ExperienceLevel.FRESHER,
            Difficulty.EASY,
            "public String reverseString(String str) {\n    // Your code here\n    return \"\";\n}",
            null,
            "Use StringBuilder or character array to reverse the string efficiently.",
            null
        );

        createQuestion(
            "Find Duplicate Elements",
            "Given an array of integers, find and return all duplicate elements.",
            QuestionType.CODING,
            domain,
            ExperienceLevel.JUNIOR,
            Difficulty.MEDIUM,
            "public List<Integer> findDuplicates(int[] nums) {\n    // Your code here\n    return new ArrayList<>();\n}",
            null,
            "Use HashSet or HashMap to track seen elements.",
            null
        );

        // MCQ Questions
        createQuestion(
            "Java Inheritance",
            "Which of the following statements about Java inheritance is true?",
            QuestionType.MCQ,
            domain,
            ExperienceLevel.FRESHER,
            Difficulty.EASY,
            null,
            "[\"Java supports multiple inheritance through classes\", \"Java supports multiple inheritance through interfaces\", \"Java does not support any form of inheritance\", \"Java inheritance is same as C++ inheritance\"]",
            "Java supports multiple inheritance through interfaces",
            false
        );

        createQuestion(
            "Spring Framework Features",
            "Which of the following are core features of Spring Framework? (Select all that apply)",
            QuestionType.MCQ,
            domain,
            ExperienceLevel.MID,
            Difficulty.MEDIUM,
            null,
            "[\"Dependency Injection\", \"Aspect-Oriented Programming\", \"Built-in Database\", \"Transaction Management\", \"Data Binding\"]",
            "Dependency Injection,Aspect-Oriented Programming,Transaction Management,Data Binding",
            true
        );

        // Subjective Questions
        createQuestion(
            "Explain Java Memory Model",
            "Describe the Java Memory Model and explain how garbage collection works.",
            QuestionType.SUBJECTIVE,
            domain,
            ExperienceLevel.MID,
            Difficulty.MEDIUM,
            null,
            null,
            "Java Memory Model includes Heap (Young and Old generation), Method Area, Stack, PC registers, and Native Method Stack. Garbage collection automatically manages memory by removing unreachable objects.",
            null
        );

        createQuestion(
            "Microservices Design Patterns",
            "What are the key design patterns used in microservices architecture and how do they solve common problems?",
            QuestionType.SUBJECTIVE,
            domain,
            ExperienceLevel.SENIOR,
            Difficulty.HARD,
            null,
            null,
            "Key patterns include Circuit Breaker, Service Discovery, API Gateway, Event Sourcing, CQRS, Saga Pattern, and Database per Service. These patterns address issues like service communication, data consistency, fault tolerance, and service coordination.",
            null
        );
    }

    private void seedPythonQuestions() {
        String domain = "Python Development";
        
        // Coding Questions
        createQuestion(
            "Python List Comprehension",
            "Create a list of squares of even numbers from 1 to 20 using list comprehension.",
            QuestionType.CODING,
            domain,
            ExperienceLevel.FRESHER,
            Difficulty.EASY,
            "# Write your list comprehension here\nresult = []",
            null,
            "Use list comprehension with conditional filtering.",
            null
        );

        // MCQ Questions
        createQuestion(
            "Python Data Types",
            "Which of the following is a mutable data type in Python?",
            QuestionType.MCQ,
            domain,
            ExperienceLevel.FRESHER,
            Difficulty.EASY,
            null,
            "[\"tuple\", \"string\", \"list\", \"frozenset\"]",
            "list",
            false
        );

        // Subjective Questions
        createQuestion(
            "Python GIL Explanation",
            "Explain what is Global Interpreter Lock (GIL) in Python and its impact on multithreading.",
            QuestionType.SUBJECTIVE,
            domain,
            ExperienceLevel.MID,
            Difficulty.MEDIUM,
            null,
            null,
            "GIL is a mutex that protects access to Python objects, preventing multiple threads from executing Python bytecode simultaneously. It limits true parallelism in CPU-bound tasks but doesn't affect I/O-bound tasks significantly.",
            null
        );
    }

    private void seedFrontendQuestions() {
        String domain = "Frontend Development";
        
        // Coding Questions
        createQuestion(
            "DOM Manipulation",
            "Write JavaScript code to change the background color of all div elements to blue.",
            QuestionType.CODING,
            domain,
            ExperienceLevel.FRESHER,
            Difficulty.EASY,
            "// Write your JavaScript code here",
            null,
            "Use document.querySelectorAll() or getElementsByTagName().",
            null
        );

        // MCQ Questions
        createQuestion(
            "CSS Flexbox",
            "Which CSS property is used to control the direction of flex items?",
            QuestionType.MCQ,
            domain,
            ExperienceLevel.JUNIOR,
            Difficulty.EASY,
            null,
            "[\"flex-direction\", \"flex-wrap\", \"justify-content\", \"align-items\"]",
            "flex-direction",
            false
        );

        // Subjective Questions
        createQuestion(
            "React Virtual DOM",
            "Explain how React's Virtual DOM works and its benefits over direct DOM manipulation.",
            QuestionType.SUBJECTIVE,
            domain,
            ExperienceLevel.MID,
            Difficulty.MEDIUM,
            null,
            null,
            "Virtual DOM is a JavaScript representation of the real DOM. React creates a virtual DOM tree, compares it with the previous version (diffing), and updates only the changed parts (reconciliation). This is faster than direct DOM manipulation and provides better performance.",
            null
        );
    }

    private void seedSystemDesignQuestions() {
        String domain = "System Design";
        
        // Subjective Questions
        createQuestion(
            "Design URL Shortener",
            "Design a URL shortening service like bit.ly. Discuss the architecture, database design, and scaling considerations.",
            QuestionType.SUBJECTIVE,
            domain,
            ExperienceLevel.SENIOR,
            Difficulty.HARD,
            null,
            null,
            "Key components: URL encoding service, database for mappings, cache layer, load balancer, analytics service. Use base62 encoding, database sharding, CDN for global distribution, and rate limiting for abuse prevention.",
            null
        );

        createQuestion(
            "Chat System Design",
            "Design a real-time chat system that can handle millions of users. Discuss the architecture and technology choices.",
            QuestionType.SUBJECTIVE,
            domain,
            ExperienceLevel.EXPERT,
            Difficulty.HARD,
            null,
            null,
            "Architecture includes WebSocket servers, message queue (Kafka), database for message storage, notification service, user presence service, load balancers, and CDN. Use horizontal scaling, message partitioning, and eventual consistency for scalability.",
            null
        );
    }

    private void createQuestion(String title, String description, QuestionType type, String domain,
                              ExperienceLevel experience, Difficulty difficulty, String starterCode,
                              String options, String correctAnswer, Boolean multipleSelection) {
        Question question = new Question(title, description, type, domain, experience);
        question.setDifficulty(difficulty);
        question.setStarterCode(starterCode);
        question.setOptions(options);
        question.setCorrectAnswer(correctAnswer);
        question.setMultipleSelection(multipleSelection);
        question.setActive(true);
        
        // Set tags based on domain and type
        StringBuilder tags = new StringBuilder();
        tags.append(domain.toLowerCase().replace(" ", "-"));
        tags.append(",").append(type.name().toLowerCase());
        tags.append(",").append(experience.name().toLowerCase());
        tags.append(",").append(difficulty.name().toLowerCase());
        question.setTags(tags.toString());
        
        questionRepository.save(question);
    }
} 