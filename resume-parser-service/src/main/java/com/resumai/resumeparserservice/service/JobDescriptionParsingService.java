package com.resumai.resumeparserservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class JobDescriptionParsingService {

    /**
     * Parse job description fields from extracted text content
     */
    public Map<String, String> parseJobDescriptionFields(String text) {
        Map<String, String> fields = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            return fields;
        }
        
        String cleanText = text.trim();
        log.info("Parsing job description from {} characters of text", cleanText.length());
        
        // Extract job title
        String title = extractJobTitle(cleanText);
        if (title != null) {
            fields.put("title", title);
        }
        
        // Extract company name
        String company = extractCompanyName(cleanText);
        if (company != null) {
            fields.put("company", company);
        }
        
        // Extract location
        String location = extractLocation(cleanText);
        if (location != null) {
            fields.put("location", location);
        }
        
        // Extract experience level
        String experienceLevel = extractExperienceLevel(cleanText);
        if (experienceLevel != null) {
            fields.put("experienceLevel", experienceLevel);
        }
        
        // Extract requirements
        String requirements = extractRequirements(cleanText);
        if (requirements != null) {
            fields.put("requirements", requirements);
        }
        
        // Extract responsibilities
        String responsibilities = extractResponsibilities(cleanText);
        if (responsibilities != null) {
            fields.put("responsibilities", responsibilities);
        }
        
        log.info("Extracted fields: {}", fields.keySet());
        return fields;
    }
    
    private String extractJobTitle(String text) {
        // Common patterns for job titles
        String[] titlePatterns = {
            "(?i)job\\s+title\\s*[:\\-]\\s*([^\n\r]{10,80})",
            "(?i)position\\s*[:\\-]\\s*([^\n\r]{10,80})",
            "(?i)role\\s*[:\\-]\\s*([^\n\r]{10,80})",
            "(?i)^\\s*([A-Z][A-Za-z\\s]{5,50}(?:Engineer|Developer|Manager|Analyst|Specialist|Lead|Director|Coordinator|Administrator))",
            "(?i)hiring\\s+for\\s*[:\\-]?\\s*([^\n\r]{10,80})"
        };
        
        for (String pattern : titlePatterns) {
            Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String title = m.group(1).trim();
                if (title.length() > 5 && title.length() < 80) {
                    return title;
                }
            }
        }
        
        // Fallback: look for the first meaningful line that might be a title
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 10 && line.length() < 80 && 
                line.matches(".*(?i)(engineer|developer|manager|analyst|specialist|lead|director|coordinator).*")) {
                return line;
            }
        }
        
        return null;
    }
    
    private String extractCompanyName(String text) {
        String[] companyPatterns = {
            "(?i)company\\s*[:\\-]\\s*([^\n\r]{2,50})",
            "(?i)organization\\s*[:\\-]\\s*([^\n\r]{2,50})",
            "(?i)at\\s+([A-Z][A-Za-z\\s&.]{2,30}(?:Inc|Ltd|LLC|Corp|Corporation|Company|Technologies|Systems|Solutions))",
            "(?i)join\\s+([A-Z][A-Za-z\\s&.]{2,40})",
            "(?i)working\\s+with\\s+([A-Z][A-Za-z\\s&.]{2,40})"
        };
        
        for (String pattern : companyPatterns) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String company = m.group(1).trim();
                if (company.length() > 2 && company.length() < 50) {
                    return company;
                }
            }
        }
        
        return null;
    }
    
    private String extractLocation(String text) {
        String[] locationPatterns = {
            "(?i)location\\s*[:\\-]\\s*([^\n\r]{2,50})",
            "(?i)based\\s+in\\s+([A-Za-z\\s,]{3,40})",
            "(?i)office\\s*[:\\-]\\s*([A-Za-z\\s,]{3,40})",
            "(?i)city\\s*[:\\-]\\s*([A-Za-z\\s,]{3,40})",
            "(?i)(Mumbai|Delhi|Bangalore|Chennai|Hyderabad|Pune|Kolkata|Ahmedabad|Gurgaon|Noida|Remote|Hybrid)",
            "(?i)([A-Z][a-z]+,\\s*[A-Z][a-z]+)"
        };
        
        for (String pattern : locationPatterns) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String location = m.group(1).trim();
                if (location.length() > 2 && location.length() < 50) {
                    return location;
                }
            }
        }
        
        return null;
    }
    
    private String extractExperienceLevel(String text) {
        String[] experiencePatterns = {
            "(?i)experience\\s*[:\\-]\\s*([0-9]+[\\-\\+]?\\s*(?:to\\s+[0-9]+)?\\s*years?)",
            "(?i)([0-9]+[\\-\\+]?\\s*(?:to\\s+[0-9]+)?\\s*years?\\s*(?:of\\s+)?experience)",
            "(?i)(fresher|entry.level|junior|senior|lead|principal|architect)",
            "(?i)minimum\\s+([0-9]+\\s*years?)",
            "(?i)at\\s+least\\s+([0-9]+\\s*years?)"
        };
        
        for (String pattern : experiencePatterns) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String exp = m.group(1).trim();
                if (exp.length() > 1 && exp.length() < 30) {
                    return exp;
                }
            }
        }
        
        return null;
    }
    
    private String extractRequirements(String text) {
        // More flexible patterns for requirements sections
        String[] requirementSections = {
            "(?i)requirements?\\s*[:\\-]\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:responsibilities|qualifications|skills|about|company|job description|what you|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)qualifications?\\s*[:\\-]\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:responsibilities|requirements|skills|about|company|job description|what you|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)must\\s+have\\s*[:\\-]?\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:responsibilities|requirements|skills|about|company|job description|what you|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)essential\\s*[:\\-]\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:responsibilities|requirements|skills|about|company|job description|what you|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)what\\s+we.*looking.*for\\s*[:\\-]?\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:responsibilities|requirements|skills|about|company|job description|what you|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)candidate\\s+should\\s*[:\\-]?\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:responsibilities|requirements|skills|about|company|job description|what you|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)skills?\\s+required\\s*[:\\-]?\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:responsibilities|requirements|about|company|job description|what you|experience|benefits|salary)|\\n\\n\\n|$)"
        };
        
        for (String pattern : requirementSections) {
            Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String req = m.group(1).trim();
                // Clean up the text
                req = req.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n"); // Remove excessive line breaks
                req = req.replaceAll("^\\s*[\\-\\*\\•]\\s*", ""); // Remove leading bullets
                
                if (req.length() > 10 && req.length() < 2000) {
                    log.info("Found requirements section with {} characters", req.length());
                    return req;
                }
            }
        }
        
        // Fallback: Look for bullet-pointed lists that might be requirements
        String[] lines = text.split("\n");
        StringBuilder requirements = new StringBuilder();
        boolean inRequirementsSection = false;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Check if this line starts a requirements section
            if (trimmedLine.toLowerCase().matches(".*(?:requirement|qualification|skill|must have|essential).*")) {
                inRequirementsSection = true;
                continue;
            }
            
            // If we're in requirements section and find a bullet point
            if (inRequirementsSection && trimmedLine.matches("^[\\-\\*\\•]\\s*.{5,}")) {
                requirements.append(trimmedLine).append("\n");
            }
            
            // Stop if we hit another section
            if (inRequirementsSection && trimmedLine.toLowerCase().matches(".*(?:responsibilit|dut|about|company|contact).*")) {
                break;
            }
        }
        
        if (requirements.length() > 20) {
            log.info("Found requirements from bullet points with {} characters", requirements.length());
            return requirements.toString().trim();
        }
        
        return null;
    }
    
    private String extractResponsibilities(String text) {
        // More flexible patterns for responsibilities
        String[] responsibilityPatterns = {
            "(?i)responsibilities\\s*[:\\-]\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:requirements|qualifications|skills|about|company|job description|what we|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)duties\\s*[:\\-]\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:requirements|qualifications|skills|about|company|job description|what we|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)role\\s+description\\s*[:\\-]\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:requirements|qualifications|skills|about|company|job description|what we|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)what\\s+you.*(?:do|will)\\s*[:\\-]?\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:requirements|qualifications|skills|about|company|job description|what we|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)your\\s+role\\s*[:\\-]?\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:requirements|qualifications|skills|about|company|job description|what we|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)job\\s+duties\\s*[:\\-]?\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:requirements|qualifications|skills|about|company|job description|what we|experience|benefits|salary)|\\n\\n\\n|$)",
            "(?i)key\\s+responsibilities\\s*[:\\-]?\\s*([\\s\\S]{20,1000}?)(?=\\n\\s*(?:requirements|qualifications|skills|about|company|job description|what we|experience|benefits|salary)|\\n\\n\\n|$)"
        };
        
        for (String pattern : responsibilityPatterns) {
            Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String resp = m.group(1).trim();
                // Clean up the text
                resp = resp.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n"); // Remove excessive line breaks
                resp = resp.replaceAll("^\\s*[\\-\\*\\•]\\s*", ""); // Remove leading bullets
                
                if (resp.length() > 10 && resp.length() < 2000) {
                    log.info("Found responsibilities section with {} characters", resp.length());
                    return resp;
                }
            }
        }
        
        // Fallback: Look for bullet-pointed lists that might be responsibilities
        String[] lines = text.split("\n");
        StringBuilder responsibilities = new StringBuilder();
        boolean inResponsibilitiesSection = false;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Check if this line starts a responsibilities section
            if (trimmedLine.toLowerCase().matches(".*(?:responsibilit|dut|role|what you).*")) {
                inResponsibilitiesSection = true;
                continue;
            }
            
            // If we're in responsibilities section and find a bullet point
            if (inResponsibilitiesSection && trimmedLine.matches("^[\\-\\*\\•]\\s*.{5,}")) {
                responsibilities.append(trimmedLine).append("\n");
            }
            
            // Stop if we hit another section
            if (inResponsibilitiesSection && trimmedLine.toLowerCase().matches(".*(?:requirement|qualification|skill|about|company|contact).*")) {
                break;
            }
        }
        
        if (responsibilities.length() > 20) {
            log.info("Found responsibilities from bullet points with {} characters", responsibilities.length());
            return responsibilities.toString().trim();
        }
        
        return null;
    }
} 