package com.camel.orden.compra.camel_order_compra.process;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessXmltoJson {
    public String convertXmlToJson(String xml) {
        try {
            List<Map<String, Object>> bodyList = new ArrayList<>();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            doc.getDocumentElement().normalize();

            NodeList bodyNodes = doc.getElementsByTagName("Body");

            for (int i = 0; i < bodyNodes.getLength(); i++) {
                Element bodyElem = (Element) bodyNodes.item(i);
                Map<String, Object> bodyMap = new LinkedHashMap<>();

                // Cliente
                bodyMap.put("cliente", bodyElem.getElementsByTagName("cliente").item(0).getTextContent());

                // Productos
                NodeList productoNodes = bodyElem.getElementsByTagName("Producto");
                List<Map<String, Object>> productos = new ArrayList<>();

                for (int j = 0; j < productoNodes.getLength(); j++) {
                    Element prodElem = (Element) productoNodes.item(j);
                    Map<String, Object> productoMap = new LinkedHashMap<>();
                    productoMap.put("nombre", prodElem.getElementsByTagName("nombre").item(0).getTextContent());
                    productoMap.put("cantidad", Integer.parseInt(prodElem.getElementsByTagName("cantidad").item(0).getTextContent()));
                    productos.add(productoMap);
                }

                bodyMap.put("productos", productos);

                // Fecha
                bodyMap.put("fecha", bodyElem.getElementsByTagName("fecha").item(0).getTextContent());

                bodyList.add(bodyMap);
            }

            // Ahora convertir List<Map> a JSON manualmente
            return listToJson(bodyList);

        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }

    }

    private String listToJson(List<Map<String, Object>> list) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            json.append(mapToJson(list.get(i)));
            if (i < list.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else if (entry.getValue() instanceof List) {
                json.append("[");
                List<?> list = (List<?>) entry.getValue();
                for (int i = 0; i < list.size(); i++) {
                    json.append(mapToJson((Map<String, Object>) list.get(i)));
                    if (i < list.size() - 1) json.append(",");
                }
                json.append("]");
            } else {
                json.append(entry.getValue());
            }
            if (++count < map.size()) json.append(",");
        }
        json.append("}");
        return json.toString();
    }
}
