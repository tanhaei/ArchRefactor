package com.tanhaei.archrefactor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Implements Algorithm 1: Graph Extraction from the ArchRefactor paper.
 * Parses Java source code to AST and generates an Architectural JSON Abstraction.
 */
public class GraphExtractor {

    public static void main(String[] args) {
        // Basic argument parsing (simplified for demo)
        if (args.length < 2) {
            System.out.println("Usage: java -jar ArchRefactor.jar --input <src_dir> --output <output.json>");
            return;
        }

        String inputPath = args[1]; // simplified
        String outputPath = args.length > 3 ? args[3] : "abstraction.json";

        try {
            setupSymbolSolver();
            JSONObject architectureGraph = extractArchitecture(inputPath);
            saveJson(architectureGraph, outputPath);
            System.out.println("Extraction Complete. Saved to: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupSymbolSolver() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        // In a real scenario, you'd add JavaParserTypeSolver for the source root
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);
    }

    private static JSONObject extractArchitecture(String rootDir) throws IOException {
        JSONObject graph = new JSONObject();
        JSONArray classesArray = new JSONArray();

        List<Path> javaFiles = Files.walk(Paths.get(rootDir))
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());

        for (Path file : javaFiles) {
            try {
                CompilationUnit cu = StaticJavaParser.parse(file);
                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cid -> {
                    JSONObject classJson = processClass(cid);
                    classesArray.put(classJson);
                });
            } catch (Exception e) {
                System.err.println("Error parsing file: " + file);
            }
        }

        graph.put("project", rootDir);
        graph.put("classes", classesArray);
        
        // Note: Louvain Community Detection logic would typically be applied here 
        // or in the Python post-processing step depending on library availability.
        
        return graph;
    }

    private static JSONObject processClass(ClassOrInterfaceDeclaration cid) {
        JSONObject json = new JSONObject();
        json.put("className", cid.getFullyQualifiedName().orElse(cid.getNameAsString()));
        json.put("isInterface", cid.isInterface());

        // Extract Methods and infer simple responsibilities (Step 7 in Alg 1)
        JSONArray methods = new JSONArray();
        cid.getMethods().forEach(m -> {
            JSONObject methodObj = new JSONObject();
            methodObj.put("name", m.getNameAsString());
            methodObj.put("lines", m.getEnd().get().line - m.getBegin().get().line);
            // Simple keyword-based responsibility inference placeholder
            methodObj.put("inferred_resp", inferResponsibility(m.getNameAsString()));
            methods.put(methodObj);
        });
        json.put("methods", methods);

        // Calculate Metrics (Simplified)
        JSONObject metrics = new JSONObject();
        metrics.put("methodCount", cid.getMethods().size());
        metrics.put("fieldCount", cid.getFields().size());
        json.put("metrics", metrics);

        // Dependencies (Simplified - strictly call/inheritance based)
        JSONArray dependencies = new JSONArray();
        if (!cid.getExtendedTypes().isEmpty()) {
            dependencies.put(new JSONObject().put("type", "inherit").put("target", cid.getExtendedTypes().get(0).getNameAsString()));
        }
        json.put("dependencies", dependencies);

        return json;
    }

    private static String inferResponsibility(String methodName) {
        String lower = methodName.toLowerCase();
        if (lower.contains("save") || lower.contains("db") || lower.contains("query")) return "Persistence";
        if (lower.contains("render") || lower.contains("view") || lower.contains("ui")) return "Presentation";
        if (lower.contains("validate") || lower.contains("auth")) return "Security";
        return "Business Logic";
    }

    private static void saveJson(JSONObject json, String path) throws IOException {
        try (FileWriter file = new FileWriter(path)) {
            file.write(json.toString(2));
        }
    }
}
