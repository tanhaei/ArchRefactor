import json
import os
import argparse
from openai import OpenAI
import networkx as nx

# Initialize OpenAI Client
client = OpenAI(api_key=os.environ.get("OPENAI_API_KEY"))

PROMPT_DETECTION = """
SYSTEM: You are a Senior Software Architect with 20+ years experience.
USER: Analyze this JSON: {JSON_INPUT}
Step 1: Infer distinct responsibilities from methods.
Step 2: Count external deps and check cohesion (LCOM-like).
Step 3: Is it a God Class? (Yes/No, explain).
Step 4: Confidence (0-1). Suggest refactor if Yes.
OUTPUT: Strict JSON: {"isSmell": bool, "confidence": float, "explanation": str, "refactor": str}
"""

PROMPT_REFACTORING = """
SYSTEM: Suggest safe refactorings for AS.
USER: For this God Class JSON: {JSON_INPUT}, propose 2-3 strategies (e.g., Extract Class).
Consider: modularity, testability.
OUTPUT: JSON array of {"operation": str, "rationale": str}
"""

def load_graph(json_path):
    with open(json_path, 'r') as f:
        data = json.load(f)
    return data

def analyze_class(class_data, mode="detection"):
    """
    Interacts with LLM to detect smells or suggest refactoring.
    """
    json_str = json.dumps(class_data)
    
    if mode == "detection":
        prompt = PROMPT_DETECTION.replace("{JSON_INPUT}", json_str)
    else:
        prompt = PROMPT_REFACTORING.replace("{JSON_INPUT}", json_str)

    try:
        response = client.chat.completions.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "You are an expert software architect helper."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.0  # Zero temperature for reproducibility as per paper
        )
        return response.choices[0].message.content
    except Exception as e:
        return f"Error: {str(e)}"

def process_project(data, output_file):
    results = []
    classes = data.get("classes", [])
    
    print(f"Analyzing {len(classes)} classes...")
    
    for cls in classes:
        # Optimization: Pre-filter small classes to save tokens (implied in paper)
        if cls["metrics"]["methodCount"] < 10: 
            continue
            
        print(f"Checking {cls['className']}...")
        result_raw = analyze_class(cls, mode="detection")
        
        # Simple parsing logic (in production, use robust JSON parsing)
        try:
            result_json = json.loads(result_raw)
            if result_json.get("isSmell"):
                print(f"(!) God Class Detected: {cls['className']} (Conf: {result_json.get('confidence')})")
                
                # If smell detected, ask for refactoring strategies
                refactor_suggestions = analyze_class(cls, mode="refactoring")
                result_json["refactoring_strategies"] = refactor_suggestions
                
                results.append({
                    "class": cls['className'],
                    "analysis": result_json
                })
        except:
            print(f"Failed to parse LLM response for {cls['className']}")

    # Save Report
    with open(output_file, 'w') as f:
        json.dump(results, f, indent=2)
    print(f"Analysis complete. Report saved to {output_file}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='ArchRefactor LLM Analyzer')
    parser.add_argument('--input', required=True, help='Path to abstraction.json')
    parser.add_argument('--output', default='report.json', help='Output report path')
    args = parser.parse_args()

    data = load_graph(args.input)
    process_project(data, args.output)
