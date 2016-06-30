package com.stroganov.jgate.codegen

import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.gradle.api.*
import org.gradle.api.tasks.TaskAction;
import org.apache.velocity.app.VelocityEngine;

/**
 * Created by stroganov on 05.06.2016.
 */
class JGateGenerateClassesTask extends DefaultTask {
    private final VelocityEngine ve;
    public JGateGenerateClassesTask() {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
    }
    @TaskAction
    def makesrc() {
        println(project.jgate.schemetool.toString());
        if (!project.jgate.schemetool.exists()) {
            throw new GradleException(project.jgate.schemetool.toString() + " does not exists");
        }

        File schemetoolExec = new File(
                project.jgate.schemetool,
                System.getProperty("os.name").toLowerCase().contains("win") ? "schemetool.exe"
                                                                            : "schemetool"
        );
        if (!schemetoolExec.exists()) {
            throw new GradleException(schemetoolExec.toString() + " does not exists");
        }
        if (!schemetoolExec.canExecute()) {
            throw new GradleException(schemetoolExec.toString() + " is not executable file");
        }
        println("schemetool: $schemetoolExec");
        File genSrcDir = new File(
                project.jgate.generatedSrcDir,
                project.jgate.userPackage.replaceAll('\\.', '/')
        );
        File schemasDir = new File(genSrcDir, "schemas");
        schemasDir.mkdirs();
        int counter = 0;
        println("Generating sources...");
        def schemas = CGateScheme.fromIterable(project.jgate.schemas);
        schemas.forEach() {
            print("Generating... ");
            def cmd = [schemetoolExec.toString()];
            cmd += 'makesrc -O java';
            File scheme = new File(schemasDir, "${it.name}.java");
            cmd += "-o $scheme";
            cmd += "-Djava-user-package=${project.jgate.userPackage}.schemas";
            cmd += "-Djava-class-name=$it.name";
            cmd += "$it.file.name $it.name";
            def stdOut = new StringBuffer();
            def errOut = new StringBuffer();
            Process result = cmd.join(' ').execute();
            result.waitForProcessOutput(stdOut, errOut);
            if (result.exitValue() == 0) {
                println("$scheme ...complete");
            } else {
                throw new GradleException(errOut.toString());
            }
            counter++;
            print("Generating... ");
            String genFileName = generateFromVelocityTempl(
                    new File(schemasDir, 'listeners'),
                    "${it.name}Listener.java",
                    'SchemeListener.vm',
                    it);
            println("$genFileName ...complete");
            counter++;
            print("Generating... ");
            genFileName = generateFromVelocityTempl(
                    new File(schemasDir, 'listeners'),
                    "${it.name}ListenerImpl.java",
                    'SchemeListenerImpl.vm',
                    it);
            println("$genFileName ...complete");
            counter++;
            print("Generating... ");
            genFileName = generateFromVelocityTempl(
                    new File(schemasDir, 'subscribers'),
                    "${it.name}Subscriber.java",
                    'SchemeSubscriber.vm',
                    it);
            println("$genFileName ...complete");
            counter++;
        }
        print("Generating... ");
        String genFileName = generateFromSchemasBuilderTempl(new File(schemasDir, 'builder'), schemas)
        println("$genFileName ...complete");
        counter++;
        println("$counter file(s) successfully generated.");
    }

    String generateFromVelocityTempl(File outputDir, String outputFileName, String template, CGateScheme scheme) {
        VelocityContext vc = new VelocityContext();
        vc.put('listenersPackage', "${project.jgate.userPackage}.schemas.listeners");
        vc.put('schemasPackage', "${project.jgate.userPackage}.schemas");
        vc.put('subscribersPackage', "${project.jgate.userPackage}.schemas.subscribers");
        vc.put('scheme', scheme);
        Template templ = ve.getTemplate(template);
        outputDir.mkdirs();
        File output = new File(outputDir, outputFileName);
        FileWriter writer = new FileWriter(output);
        writer.withCloseable {
            templ.merge(vc, writer);
        }
        return output.toString();
    }

    String generateFromSchemasBuilderTempl(File outputDir, Iterable<CGateScheme> schemas) {
        VelocityContext vc = new VelocityContext();
        vc.put('listenersPackage', "${project.jgate.userPackage}.schemas.listeners");
        vc.put('schemasPackage', "${project.jgate.userPackage}.schemas");
        vc.put('subscribersPackage', "${project.jgate.userPackage}.schemas.subscribers");
        vc.put('schemas', schemas);
        Template templ = ve.getTemplate('SchemasBuilder.vm');
        outputDir.mkdirs();
        File output = new File(outputDir, 'SchemasBuilder.java');
        FileWriter writer = new FileWriter(output);
        writer.withCloseable {
            templ.merge(vc, writer);
        }
        return output.toString();
    }
}
