package co.com.pragma.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Solicitudes",
                version = "4.0",
                description = "Esta API permite la gestión de solicitudes y estados asociados.",
                contact = @Contact(
                        name = "Rober Sehuanez",
                        url = "https://www.pragma.com.co",
                        email = "contacto@pragma.com.co"
                ),
                license = @License(
                        name = "Licencia Pragma",
                        url = "https://www.pragma.com.co"
                )
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Ingresa tu JWT (JSON Web Token) en el campo."
)
public class OpenApiConfiguration {
}
