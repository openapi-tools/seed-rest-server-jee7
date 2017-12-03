package io.openapitools.sample.openapi;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;


/**
 * a reader listener that can create information into the swagger.json or swagger.yaml files
 * it is included as an example of a ReaderListener, the two tags might as well have been defined
 * in the ServicesApplication openapi annotation.
 */
@SwaggerDefinition
public class LocalReaderListener implements ReaderListener {

    @Override
    public void beforeScan(Reader reader, Swagger swagger) {
        ExternalDocs immutableDocs = new ExternalDocs("Immutability described at: ",
                "https://en.wikipedia.org/wiki/Immutable_object");
        Tag immutable = new Tag()
                .name("immutable")
                .description("immutable means the object/resource never changes")
                .externalDocs(immutableDocs);
        swagger.addTag(immutable);
        ExternalDocs decoratorDocs = new ExternalDocs("Decorator pattern described at: ",
                "https://en.wikipedia.org/wiki/Decorator_pattern");
        Tag decorator = new Tag()
                .name("decorator")
                .description("decorator is an a pattern that allows other objects/resources to preserve their nature, " +
                        "e.g. stay immutable, stay authoritative etc. by wrapping these other objects/resources")
                .externalDocs(decoratorDocs);
        swagger.addTag(decorator);
        swagger.addTag(new Tag().name("PartlyGenerated").description("The API is partly generated"));
    }

    @Override
    public void afterScan(Reader reader, Swagger swagger) {
        Map<String, Path> paths = swagger.getPaths();
        paths.forEach((k, p) -> {
            List<Operation> operations = p.getOperations();
            operations.forEach(operation -> {
                SecuritySchemes.addStandardSecuritySchemes(operation);
                Responses.addStandardResponses(operation);
                Headers.addStandardParameters(operation);
                Responses.addVerbSpecificHeaders(p);
            });
        });
    }
}
