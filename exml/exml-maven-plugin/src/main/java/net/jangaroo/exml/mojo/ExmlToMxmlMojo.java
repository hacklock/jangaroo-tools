/*
 * Copyright (c) 2009, CoreMedia AG, Hamburg. All rights reserved.
 */
package net.jangaroo.exml.mojo;

import net.jangaroo.exml.compiler.Exmlc;
import net.jangaroo.exml.config.ExmlConfiguration;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Mojo to compile EXML sources to AS3 sources into target/generated-sources/joo in phase generate-sources.
 *
 * @goal convert-to-mxml
 * @phase generate-sources
 * @requiresDependencyResolution
 * @threadSafe
 */
public class ExmlToMxmlMojo extends AbstractExmlMojo {

  /**
   * Source directory to scan for test files to compile.
   *
   * @parameter expression="${project.build.testSourceDirectory}"
   */
  private File testSourceDirectory;

  @Override
  public void execute() throws MojoExecutionException {
    // Convert main EXML sources to MXML:
    ExmlConfiguration config = createExmlConfiguration(getMavenPluginHelper().getActionScriptClassPath(false),
            Collections.singletonList(getSourceDirectory()), getSourceDirectory());
    new Exmlc(config).convertAllExmlToMxml();
    // Also convert test EXML sources to MXML:
    if (testSourceDirectory != null && testSourceDirectory.exists()) {
      ExmlConfiguration testConfig = createExmlConfiguration(getActionScriptTestClassPath(),
              Collections.singletonList(testSourceDirectory), testSourceDirectory);
      new Exmlc(testConfig).convertAllExmlToMxml();
    }
  }

  private List<File> getActionScriptTestClassPath() {
    final List<File> classPath = new ArrayList<File>(getMavenPluginHelper().getActionScriptClassPath(true));
    classPath.add(0, getSourceDirectory());
    return classPath;
  }
}