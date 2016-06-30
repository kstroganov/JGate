package com.stroganov.jgate.codegen

import java.util.stream.StreamSupport

/**
 * Created by stroganov on 18.06.2016.
 */
class CGateScheme {
    private final String name;
    private final File file;
    private CGateScheme(String name, File file) {
        this.name = name;
        this.file = file;
    }
    public static Iterable<CGateScheme> fromIterable(Iterable<File> schemeFiles) {
        return new Iterable<CGateScheme>() {
            @Override
            Iterator<CGateScheme> iterator() {
                StreamSupport.stream(schemeFiles.spliterator(), false).flatMap { file ->
                    file.readLines().stream().filter {
                        it.find(/\[dbscheme:(\w+)\]/) != null
                    }.map {
                        new CGateScheme(
                                it.find(/\[dbscheme:(\w+)\]/) { match, schemeName -> schemeName; },
                                file
                        );
                    }
                }.iterator();
            }
        }
    }
    public String getName() { return name; }
    public File getFile() { return file; }
    public Iterable<String> getTables() {
        return new Iterable<String>() {
            @Override
            Iterator<String> iterator() {
                file.readLines().stream().filter { it.find(/\[table:$name:(\w+)\]/) != null }
                    .map { it.find(/\[table:$name:(\w+)\]/) { match, tableName -> tableName } }
                    .iterator();
            }
        }
    }
}
