# Java to C++ Transpiler
By [Jenna Denker](https://www.github.com/jndkr), [Amritanshu Kajaria](https://www.github.com/AmritanshuKajaria),
[Paul Merritt](https://www.github.com/pdm309), [Jason Yao](https://www.github.com/JasonYao),
[James Zhang](https://www.github.com/Jamez852)

## Description
This is our team's implementation of a Java 7 to C++
transpiler for the CSCI-480: Object Oriented Programming
(OOP) class.

Input: Java 7 source code with inheritance
& virtual methods

Output: C++ source code without inheritance

The [Speaker](https://www.github.com/JasonYao) is responsible for the upkeep
of the team and all project documentation.

## Goals
- Implement software using OOP design principles
- Use real software development tools
- Build a large software program test-suite
- Understand OO primitives
	- Understand and implement virtual method dispatch

## Project Map
```
/
├── README.md
│
├── build.sbt (managed library dependencies and c++ compilation configuration)
│
├── .sbtrc (like bash aliases but for sbt)
│
├── .gitignore (prevent certain files from being commmited to the git repo)
│
├── docs/ (contains all documents relevant to the project)
│   └── memos/ (contains all weekly memo source .tex and .pdf files)
│
├── lib/ (unmanaged library dependencies, like xtc and its source) 
│
├── logs/ (logger output)
│   └── xtc.log 
│
├── output/ (target c++ source & supporting java_lang library)
│   ├── java_lang.cpp
│   ├── java_lang.h
│   ├── main.cpp
│   ├── output.cpp
│   └── output.h
│
├── project/ (sbt configuration, shouldn't need to be touched)
│
├── schema/ (ast schema & examples)
│   ├── cpp.ast
│   └── inheritance.ast
│
└── src/ 
    ├── main/
    │   ├── java/
    │   │   └── edu/ (translator source code)
    │   └── resources/
    │       └── xtc.properties (translator properties file)
    └── test/
        └── java/
            ├── edu/ (translator unit tests)
            └── inputs/ (translator test inputs)
```

## Example commands

### Get into the `sbt` interpreter
```sh
sbt
```

### Run all tests
```sh
sbt test
```

### Run the translator for a single file (kicks off orchestrator)
```sh
sbt "runxtc -translate <source_file>"
# e.g.
sbt "runxtc -translate src/test/java/inputs/Test000/Test000.java"
```

### Pretty print a Java AST from a source file
```sh
sbt "runxtc -printJavaAst <source_file>"
# e.g.
sbt "runxtc -printJavaAst src/test/java/inputs/Test000/Test000.java"
```

### PHASE 1: Generate and print a single Java AST, including dependencies (for debugging purposes)
```sh
sbt "runxtc -printPhase1AST <input_file>"
# e.g.
sbt "runxtc -printPhase1AST src/test/java/inputs/test000/Test000.java"
```

### PHASE 1.5: Generate and print out the mangled Java AST
```sh
sbt "runxtc -mangleAST <input_file>"
# e.g.
sbt "runxtc -mangleAST src/test/java/inputs/test000/Test000.java"
```

### PHASE 2a: Generate and print the C++ AST data gathered
```sh
sbt "runxtc -printPhase2Data <input_file>"
# e.g.
sbt "runxtc -printPhase2Data src/test/java/inputs/test000/Test000.java"
```

### PHASE 2b: Generate and print the C++ header AST, including dependencies (for debugging purposes)
```sh
sbt "runxtc -printPhase2AST <input_file>"
# e.g.
sbt "runxtc -printPhase2AST src/test/java/inputs/test000/Test000.java"
```

### PHASE 3: Print out the C++ header file
```sh
sbt "runxtc -printPhase3HeaderFile <input_file>"
# e.g.
sbt "runxtc -printPhase3HeaderFile src/test/java/inputs/test000/Test000.java"
```

### PHASE 4: Generate and print the mutated C++ AST
```sh
sbt "runxtc -printPhase4MutatedAST <input_file>"
# e.g.
sbt "runxtc -printPhase4MutatedAST src/test/java/inputs/test000/Test000.java"
```

### PHASE 5a: Print out the `output.cpp` implementation file
```sh
sbt "runxtc -printPhase5ImplementationFile <input_file>"
# e.g.
sbt "runxtc -printPhase5ImplementationFile src/test/java/inputs/test000/Test000.java"
```

### PHASE 5b: Print out the `main.cpp` main file
```sh
sbt "runxtc -printPhase5MainFile <input_file>"
# e.g.
sbt "runxtc -printPhase5MainFile src/test/java/inputs/test000/Test000.java"
```

## Testing commands
### PHASE 1
```sh
sbt "test-only *Phase1LoadASTUnitTest*"
```

### PHASE 2
```sh
sbt "test-only *Phase2BuildAST*"
```

### PHASE 3
```sh
sbt "test-only *Phase3GenerateHeaderFile*"
```
