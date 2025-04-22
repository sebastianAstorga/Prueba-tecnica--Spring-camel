package com.camel.orden.compra.camel_order_compra.route;

import com.camel.orden.compra.camel_order_compra.dto.Order;
import com.camel.orden.compra.camel_order_compra.dto.OrderXml;
import com.camel.orden.compra.camel_order_compra.process.ProcessXmltoJson;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.camel.orden.compra.camel_order_compra.process.ProcessJson;

@Component
public class FileOrderRoute extends RouteBuilder {
    Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
    @Autowired
    ProcessJson processor;

    @Autowired
    ProcessXmltoJson processJson;

    @Value("${camel.file.input}")
    private String input;

    @Value("${camel.file.output}")
    private String output;

    @Value("${camel.file.logs}")
    private String logsFichero;
    @Override
    public void configure() throws Exception {
        from("file://" + input)
                .choice()
                // Validar archivo JSON
                .log("${headers}")
                .when(header("CamelFileName").endsWith(".json"))
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);

                    // Validación de JSON
                    if (!processor.validateJson(body)) {
                        throw new IllegalArgumentException("Formato JSON incorrecto");
                    }

                }).to("direct:PostJson")
                // Validar archivo XML
                .when(header("CamelFileName").endsWith(".xml"))
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);

                    // Validación de XML
                    if (!processor.validateXml(body)) {
                        throw new IllegalArgumentException("Formato XML incorrecto");
                    }
                }).to("direct:PostXml")
                // Si el formato no es JSON ni XML
                .otherwise()
                    .log("Formato no soportado: ${body}")
                .end()
                .to("file://"+ output+"?fileName=outputFile.json");

        restConfiguration().host("localhost").port(8080);
        from("direct:PostJson")
                .log("JSON sin corchetes: ${body}")
                .setHeader("Content-Type", constant("application/json"))
                .onException(Exception.class)
                .log("Error en la llamada REST: ${exception.message}")
                .handled(true)
                .setBody(constant("Error en el servicio REST, intenta más tarde"))
                .end()
                .to("rest:post:/api/v1/procesarOrden")
                .log(LoggingLevel.INFO, logger, "Respuesta del servicio REST: ${body}\"")
                .setHeader(Exchange.FILE_NAME, simple("respuesta-${date:now:yyyyMMdd-HHmmss}.log"))
                .to("file://"+ logsFichero);


        from("direct:PostXml")
                .convertBodyTo(String.class)
                .process(exchange -> {
                    String xml = exchange.getIn().getBody(String.class);
                    String json = processJson.convertXmlToJson(xml);
                    exchange.getIn().setBody(json);
                })
                .log(LoggingLevel.INFO, logger, "Respuesta del servicioxml: ${body}")
                .to("direct:PostJson");
    }

}

