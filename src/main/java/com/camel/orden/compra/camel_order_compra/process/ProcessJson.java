package com.camel.orden.compra.camel_order_compra.process;

import javax.json.*;
import javax.xml.parsers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProcessJson  {

    public Boolean validateJson(String json){
        try {
            json = json.trim();

            if (!json.startsWith("[") || !json.endsWith("]")) {
                return false;
            }

            json = json.substring(1, json.length() - 1).trim();
            List<String> jsonObjects = splitJsonArray(json);

            for (String obj : jsonObjects) {
                if (!validateJsonObject(obj)) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean validateJsonObject(String jsonObject) {
        jsonObject = jsonObject.trim();
        if (!jsonObject.startsWith("{") || !jsonObject.endsWith("}")) {
            return false;
        }

        jsonObject = jsonObject.substring(1, jsonObject.length() - 1).trim();
        List<String> keyValuePairs = splitKeyValuePairs(jsonObject);

        boolean hasCliente = false;
        boolean hasFecha = false;
        boolean hasProductos = false;
        boolean productosVacios = true;

        for (String pair : keyValuePairs) {
            String[] parts = pair.split(":", 2);
            if (parts.length != 2) continue;

            String key = parts[0].trim().replaceAll("^\"|\"$", "");
            String value = parts[1].trim();

            switch (key) {
                case "cliente":
                    hasCliente = !value.equals("null") && !value.replaceAll("\"", "").isEmpty();
                    break;
                case "fecha":
                    hasFecha = !value.equals("null") && !value.replaceAll("\"", "").isEmpty();
                    break;
                case "productos":
                    hasProductos = value.startsWith("[") && value.endsWith("]");
                    productosVacios = value.length() <= 2; // "[]"
                    break;
            }
        }

        return hasCliente && hasFecha && hasProductos && !productosVacios;
    }

    private List<String> splitJsonArray(String jsonArray) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int startIndex = 0;
        boolean insideString = false;

        for (int i = 0; i < jsonArray.length(); i++) {
            char c = jsonArray.charAt(i);

            if (c == '"') {
                insideString = !insideString;
            }

            if (!insideString) {
                if (c == '{') {
                    if (braceCount == 0) {
                        startIndex = i;
                    }
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        objects.add(jsonArray.substring(startIndex, i + 1));
                    }
                }
            }
        }

        return objects;
    }

    private List<String> splitKeyValuePairs(String jsonObject) {
        List<String> keyValuePairs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideString = false;
        int braceLevel = 0;
        int bracketLevel = 0;

        for (int i = 0; i < jsonObject.length(); i++) {
            char c = jsonObject.charAt(i);

            if (c == '"') {
                insideString = !insideString;
            }

            if (!insideString) {
                if (c == '{') braceLevel++;
                else if (c == '}') braceLevel--;
                else if (c == '[') bracketLevel++;
                else if (c == ']') bracketLevel--;
                else if (c == ',' && braceLevel == 0 && bracketLevel == 0) {
                    keyValuePairs.add(current.toString().trim());
                    current.setLength(0);
                    continue;
                }
            }

            current.append(c);
        }

        if (current.length() > 0) {
            keyValuePairs.add(current.toString().trim());
        }

        return keyValuePairs;
    }

    public boolean validateXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            try (InputStream is = new ByteArrayInputStream(xml.getBytes())) {
                Document document = builder.parse(is);
                document.getDocumentElement().normalize();

                NodeList bodyNodes = document.getElementsByTagName("Body");
                if (bodyNodes.getLength() == 0) return false;

                for (int i = 0; i < bodyNodes.getLength(); i++) {
                    Element body = (Element) bodyNodes.item(i);

                    // Validar cliente
                    NodeList clienteNodes = body.getElementsByTagName("cliente");
                    if (clienteNodes.getLength() == 0 || clienteNodes.item(0).getTextContent().trim().isEmpty()) {
                        return false;
                    }

                    // Validar fecha
                    NodeList fechaNodes = body.getElementsByTagName("fecha");
                    if (fechaNodes.getLength() == 0 || fechaNodes.item(0).getTextContent().trim().isEmpty()) {
                        return false;
                    }

                    // Validar productos
                    NodeList productoNodes = body.getElementsByTagName("Producto");
                    if (productoNodes.getLength() == 0) return false;

                    for (int j = 0; j < productoNodes.getLength(); j++) {
                        Element producto = (Element) productoNodes.item(j);

                        String nombre = producto.getElementsByTagName("nombre").item(0).getTextContent().trim();
                        String cantidadStr = producto.getElementsByTagName("cantidad").item(0).getTextContent().trim();

                        if (nombre.isEmpty()) return false;
                        try {
                            Integer.parseInt(cantidadStr);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                }

                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
