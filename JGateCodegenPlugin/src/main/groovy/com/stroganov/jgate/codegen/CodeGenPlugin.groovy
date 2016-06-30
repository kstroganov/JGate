package com.stroganov.jgate.codegen;

import org.gradle.api.*
import org.gradle.api.file.FileCollection;

class CodeGenExtension {
    File schemetool;
    File generatedSrcDir;
    FileCollection schemas;
    String userPackage;
}

class CodeGenPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("jgate", CodeGenExtension);
        project.task("generateJGateClasses", type: JGateGenerateClassesTask);
    }
}