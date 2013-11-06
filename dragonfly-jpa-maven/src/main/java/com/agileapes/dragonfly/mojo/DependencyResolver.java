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

package com.agileapes.dragonfly.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.traversal.CollectingDependencyNodeVisitor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/13, 0:24)
 */
@Service
public class DependencyResolver {

    private Set<File> files = null;

    public Collection<File> resolve(PluginExecutor executor) throws DependencyTreeBuilderException {
        if (files != null) {
            return files;
        }
        ArtifactFilter artifactFilter = new ScopeArtifactFilter(null);
        final DependencyNode rootNode = executor.getTreeBuilder().buildDependencyTree(executor.getProject(),
                executor.getLocalRepository(), executor.getArtifactFactory(), executor.getArtifactMetadataSource(),
                artifactFilter, executor.getArtifactCollector());

        CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
        rootNode.accept(visitor);
        final List<DependencyNode> nodes = visitor.getNodes();
        files = new HashSet<File>();
        for (DependencyNode node : nodes) {
            final Artifact artifact = node.getArtifact();
            if (artifact.getFile() != null) {
                files.add(artifact.getFile());
            } else {
                final File file = new File(executor.getLocalRepository().getBasedir() + File.separator + executor.getLocalRepository().pathOf(artifact));
                if (file.exists()) {
                    files.add(file);
                }
            }
        }
        return files;
    }

}
