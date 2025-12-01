# **ArchRefactor: A Hybrid Framework for Automated Detection and Refactoring of Architectural Smells**

**ArchRefactor** is a novel framework that integrates **Static Analysis** with **Large Language Models (LLMs)** to detect and resolve architectural debts such as God Classes and Cyclic Dependencies.

This repository contains the implementation of the approach described in the paper:

**ArchRefactor: A Hybrid Framework for Automated Detection and Refactoring of Architectural Smells** \> *Mohammad Tanhaei* \> *To Appear, 2025*

## **🚀 Features**

* **Context-Aware Architectural Abstraction:** Converts complex Java source code into token-efficient JSON graphs using JavaParser and Community Detection (Louvain Algorithm).  
* **Hybrid Detection:** Combines static metrics (CBO, LCOM) with semantic reasoning of LLMs (GPT-4).  
* **Automated Refactoring:** Generates actionable refactoring strategies (e.g., Extract Class) validated by architectural context.

## **🛠️ Project Structure**

The project is divided into two main modules:

1. **Graph Extractor (Java):** Parses source code and generates the architectural JSON abstraction.  
2. **LLM Analyzer (Python):** Consumes the JSON, interacts with the OpenAI API, and produces detection/refactoring reports.

```
ArchRefactor/  
├── src/  
│   ├── main/java/       \# JavaParser logic for AST analysis  
│   └── main/python/     \# LLM interaction and prompt engineering  
├── data/                \# Sample input/output JSONs  
├── prompts/             \# System prompts for Detection and Refactoring  
├── pom.xml              \# Maven dependencies for Java  
├── requirements.txt     \# Python dependencies  
└── README.md
```

## **📋 Prerequisites**

* **Java JDK 11+**  
* **Maven**  
* **Python 3.8+**  
* **OpenAI API Key** (GPT-4 access recommended)

## **📦 Installation**

### **1\. Build the Graph Extractor**

```
mvn clean package
```

### **2\. Install Python Dependencies**

```
pip install \-r requirements.txt
```

## **💻 Usage**

### **Step 1: Extract Architecture**

Run the Java tool to parse your project and generate the abstraction JSON.

```
java \-jar target/ArchRefactor-1.0.jar \--input /path/to/target/project \--output data/abstraction.json
```

### **Step 2: Analyze with LLM**

Set your API key and run the analyzer.

```
export OPENAI\_API\_KEY='your-api-key-here'  
python src/main/python/main.py \--input data/abstraction.json \--mode detection
```

## **📝 Citation**

If you use this tool or dataset in your research, please cite our paper:


## **📄 License**

This project is licensed under the MIT License \- see the [LICENSE](https://github.com/tanhaei/ArchRefactor/LICENSE) file for details.
