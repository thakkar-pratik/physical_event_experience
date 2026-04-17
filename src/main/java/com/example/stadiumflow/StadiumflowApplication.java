package com.example.stadiumflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * StadiumFlow Application - Intelligent Stadium Management System
 *
 * <p>Main entry point for the StadiumFlow Spring Boot application.
 * This system provides real-time crowd management, AI-powered recommendations,
 * and analytics for stadium operations during large-scale events.</p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Real-time IoT sensor data streaming via Server-Sent Events (SSE)</li>
 *   <li>AI-powered query processing using Google Gemini API and Vertex AI</li>
 *   <li>Cloud-native deployment on Google Cloud Run</li>
 *   <li>Analytics and insights with Cloud Storage integration</li>
 *   <li>Automated crowd density monitoring and recommendations</li>
 * </ul>
 *
 * <h2>Google Cloud Platform Integration:</h2>
 * <ul>
 *   <li>Cloud Run - Serverless container deployment</li>
 *   <li>Gemini API - Generative AI for natural language processing</li>
 *   <li>Vertex AI - Machine learning platform</li>
 *   <li>Cloud Storage - Analytics data persistence</li>
 *   <li>Cloud IAM - Identity and access management</li>
 *   <li>Cloud Build - CI/CD automation</li>
 *   <li>Cloud Logging - Centralized logging and monitoring</li>
 * </ul>
 *
 * <h2>System Architecture:</h2>
 * <p>The application follows a layered architecture:</p>
 * <pre>
 * Controllers (REST API) → Services (Business Logic) → Repositories (Data Access)
 * </pre>
 *
 * <h2>Configuration:</h2>
 * <p>The application supports multiple profiles:</p>
 * <ul>
 *   <li><code>default</code> - Local development with H2 database</li>
 *   <li><code>prod</code> - Production deployment with Google Cloud services</li>
 * </ul>
 *
 * <h2>Scheduling:</h2>
 * <p>Enables scheduled tasks for:</p>
 * <ul>
 *   <li>Periodic analytics snapshot creation</li>
 *   <li>Data cleanup and archiving</li>
 *   <li>Health check monitoring</li>
 * </ul>
 *
 * @author StadiumFlow Team
 * @version 1.0.0
 * @since 2026-04-16
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.scheduling.annotation.EnableScheduling
 */
@SpringBootApplication
@EnableScheduling
public class StadiumflowApplication {

    /**
     * Main entry point for the StadiumFlow application.
     *
     * <p>Initializes the Spring Boot application context and starts the embedded
     * Tomcat server to serve REST API endpoints.</p>
     *
     * @param args Command-line arguments passed to the application.
     *             Supports standard Spring Boot arguments like:
     *             <ul>
     *               <li><code>--spring.profiles.active=prod</code> - Set active profile</li>
     *               <li><code>--server.port=8080</code> - Change server port</li>
     *               <li><code>--logging.level.root=DEBUG</code> - Set log level</li>
     *             </ul>
     */
    public static void main(String[] args) {
        SpringApplication.run(StadiumflowApplication.class, args);
    }

}
