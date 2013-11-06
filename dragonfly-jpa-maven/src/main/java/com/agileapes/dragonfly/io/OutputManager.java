/*
 * Copyright (c) 2013 AgileApes, Ltd.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the
 * Software.
 */

package com.agileapes.dragonfly.io;

import com.agileapes.couteau.maven.mojo.PluginExecutorAware;
import com.agileapes.dragonfly.mojo.PluginExecutor;
import org.apache.maven.project.MavenProject;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/13, 1:14)
 */
@Component
public class OutputManager implements PluginExecutorAware<PluginExecutor> {

    private MavenProject project;

    @Override
    public void setPluginExecutor(PluginExecutor executor) {
        project = executor.getProject();
    }

    private File prepareFile(String path) throws IOException {
        final File file = new File(path);
        final File parentFile = file.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException("Failed to create parent directory: " + parentFile.getAbsolutePath());
        }
        return file;
    }

    private void writeFile(String path, byte[] bytes) throws IOException {
        final FileOutputStream stream = new FileOutputStream(prepareFile(path), false);
        stream.write(bytes, 0, bytes.length);
        stream.close();
    }

    private void writeFile(String path, String content) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(prepareFile(path)));
        writer.write(content);
        writer.close();
    }

    public void writeSourceFile(String path, byte[] bytes) throws IOException {
        writeFile(project.getBasedir() + File.separator + path, bytes);
    }

    public void writeSourceFile(String path, String content) throws IOException {
        writeFile(project.getBasedir() + File.separator + path, content);
    }

    public void writeOutputFile(String path, byte[] bytes) throws IOException {
        writeFile(project.getBuild().getOutputDirectory() + File.separator + path, bytes);
    }

    public void writeOutputFile(String path, String content) throws IOException {
        writeFile(project.getBuild().getOutputDirectory() + File.separator + path, content);
    }

    public void deleteOutput(String path) {
        if (!new File(project.getBuild().getOutputDirectory() + File.separator + path).delete()) {
            throw new Error("Failed to delete file " + path);
        }
    }

}
