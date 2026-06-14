package com.jiteen.claims.claim.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Enterprise configuration class for establishing OpenAPI 3 specification definitions
 * and interactive Swagger documentation UI for the Claim Microservice.
 * <p>
 * This class abstracts and constructs the baseline metadata parameters required by the 
 * {@code springdoc-openapi-starter-webmvc-ui} engine to dynamically collect runtime endpoint maps,
 * JSR-380 validation definitions, and schema controls into a centralized, discoverable JSON/YAML document.
 * </p>
 * <p>
 * By enforcing standard contract-first API definitions at this layer, the microservice architecture ensures:
 * </p>
 * <ul>
 * <li><strong>Enhanced Discoverability:</strong> Downstream internal microservices, edge proxies, and API gateways 
 * can programmatically consume or parse structural service capability updates without structural coupling.</li>
 * <li><strong>Optimized Developer Experience (DX):</strong> Frontend and integration engineering tracks receive 
 * an immediate, production-grade sandbox ui to safely perform execution validation, manual boundary validation checks, 
 * and trace request/response structural mappings.</li>
 * <li><strong>Ecosystem Security Mapping:</strong> Standardizes model definitions, interface expectations, 
 * and protocol rules ahead of automated orchestration loops.</li>
 * </ul>
 *
 * @author Jiteen
 * @version v1
 * @since Java 21
 */
@Configuration
public class OpenApiConfig {

    /**
     * Introspects, instantiates, and registers a global {@link OpenAPI} context container component 
     * within the Spring Application Context.
     * <p>
     * This bean compiles metadata profiles regarding authorship contact structures, open-source licensing constraints, 
     * service qualitative logs, and physical targeting servers to format and baseline the resulting contract payload.
     * </p>
     *
     * @return a fully articulated, production-ready {@link OpenAPI} contract mapping specification instance
     */
    @Bean
    public OpenAPI claimServiceOpenApi() {
        Contact apiContact = new Contact()
                .name("Jiteen Mohanty")
                .email("jiteen92718@gmail.com");

        License apiLicense = new License()
                .name("Apache License 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info apiInfo = new Info()
                .title("AI Claims Processing Platform - Claim Service API")
                .version("v1")
                .description("REST APIs responsible for insurance claim submission, retrieval, lifecycle management, "
                        + "workflow transitions, and future AI-powered claim processing operations.")
                .contact(apiContact)
                .license(apiLicense);

        Server localDevelopmentServer = new Server()
                .url("http://localhost:8082")
                .description("Local Claim Service Environment");

        return new OpenAPI()
                .info(apiInfo)
                .servers(List.of(localDevelopmentServer));
    }
}