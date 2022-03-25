package com.analysis;

import java.io.File;

public class Generator {

    /**
     * Test method only!
     * TODO: remove method
     */
    public void generate() {

    }

    /**
     * Generate all stepfiles
     * @param targetDir directory of target source code
     * @param analysisNLP location of json of nlp analysis (from src dir)
     */
    public void generate(File targetDir, String analysisNLP ) {
        System.out.printf("Generating for directory %s%n:", targetDir.getPath());
        System.out.println();
    }

    public static void main(String[] args) {
        new Generator().generate();
    }
}
