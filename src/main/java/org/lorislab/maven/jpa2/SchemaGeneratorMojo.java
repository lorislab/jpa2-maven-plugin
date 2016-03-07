/*
 * Copyright 2015 Andrej Petras.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lorislab.maven.jpa2;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Persistence;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.lorislab.maven.jpa2.persistence.PersistenceModel;
import org.lorislab.maven.jpa2.persistence.PersistenceModel21;
import org.lorislab.maven.jpa2.util.XMLUtil;

/**
 * The JPA2 generated schema.
 *
 * @author Andrej Petras
 */
@Mojo(name = "generate", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true, requiresProject = true)
@Execute(goal = "generate", phase = LifecyclePhase.PREPARE_PACKAGE)
public class SchemaGeneratorMojo extends AbstractMojo {

    /**
     * The persistence modifier.
     */
    private static final Map<String, PersistenceModel> MODIFIER = new HashMap<>();

    /**
     * Persistence version.
     */
    static {
        PersistenceModel21 p21 = new PersistenceModel21();
        MODIFIER.put(p21.getVersion(), p21);
    }

    /**
     * The MAVEN project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The persistence unit.
     */
    @Parameter
    private String persistenceUnit;

    /**
     * The database product name;
     */
    @Parameter(required = true)
    private String databaseProductName;

    /**
     * The database major version.
     */
    @Parameter(defaultValue = "")
    private String databaseMajorVersion;

    /**
     * The database minor version.
     */
    @Parameter(defaultValue = "")
    private String databaseMinorVersion;

    /**
     * The script action. Default: drop-and-create
     */
    @Parameter(defaultValue = "drop-and-create")
    private String scriptAction;

    /**
     * The output target directory. Default: generated-schema.
     */
    @Parameter(defaultValue = "generated-schema")
    private String outputTargetDir;

    /**
     * The drop target file. Default: drop.sql.
     */
    @Parameter(defaultValue = "drop.sql")
    private String dropTargetFile;

    /**
     * The create target file. Default: create.sql.
     */
    @Parameter(defaultValue = "create.sql")
    private String createTargetFile;

    /**
     * The SQL delimiter. Default ;
     */
    @Parameter(defaultValue = ";")
    private String delimiter;
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String persistentUnit = getPersistentUnit();

        Path buildDir = Paths.get(project.getBuild().getDirectory());
        Path outputDir = buildDir.resolve(outputTargetDir);
        Path dropFile = outputDir.resolve(dropTargetFile);
        Path createFile = outputDir.resolve(createTargetFile);

        Map properties = new HashMap();

        properties.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
        properties.put("javax.persistence.jtaDataSource", null);
        properties.put("javax.persistence.nonJtaDataSource", null);
        properties.put("javax.persistence.validation.mode", "NONE");

        properties.put("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
        properties.put("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:testdb");
        properties.put("javax.persistence.jdbc.user", "");
        properties.put("javax.persistence.jdbc.password", "");

        properties.put("javax.persistence.database-product-name", databaseProductName);
        properties.put("javax.persistence.database-major-version", databaseMajorVersion);
        properties.put("javax.persistence.database-minor-version", databaseMinorVersion);

        properties.put("javax.persistence.schema-generation.scripts.action", scriptAction);
        properties.put("javax.persistence.schema-generation.scripts.drop-target", dropFile.toString());
        properties.put("javax.persistence.schema-generation.scripts.create-target", createFile.toString());

        // hibernate delimiter
        properties.put("hibernate.hbm2ddl.delimiter", delimiter);
        
        final Thread currentThread = Thread.currentThread();
        final ClassLoader oldClassLoader = currentThread.getContextClassLoader();

        final ClassLoader cl = getClassLoader(oldClassLoader);
        currentThread.setContextClassLoader(cl);
        Persistence.generateSchema(persistentUnit, properties);
        currentThread.setContextClassLoader(oldClassLoader);      
    }

    private String getPersistentUnit() throws MojoExecutionException {
        if (persistenceUnit == null || persistenceUnit.isEmpty()) {

            // build directory: target
            Path buildClassDir = Paths.get(project.getBuild().getOutputDirectory());

            Path persistenceFile = buildClassDir.resolve("META-INF\\persistence.xml");

            String version = XMLUtil.getXMLVersion(persistenceFile);
            getLog().info("Version of the persistence.xml : " + persistenceFile.toString() + " version: " + version);

            final PersistenceModel model = MODIFIER.get(version);
            if (model == null) {
                throw new MojoExecutionException("Missing the persistence.xml modifier for the version: " + version);
            }

            model.loadPersistence(persistenceFile);
            List<String> units = model.getPersistenceUnits();
            if (units == null) {
                throw new MojoExecutionException("Missing the persistence units in the persistence.xml");
            }

            if (units.size() > 1) {
                throw new MojoExecutionException("Find more persistence units please use the 'persistenceUnit' attribute.");
            }

            persistenceUnit = units.get(0);
        }
        return persistenceUnit;
    }

    @SuppressWarnings("unchecked")
    private ClassLoader getClassLoader(final ClassLoader delegate) {
        try {
            final List<String> classpathElements = new ArrayList<>();
            classpathElements.addAll(project.getCompileClasspathElements());
            classpathElements.addAll(project.getRuntimeClasspathElements());
            classpathElements.add(project.getBuild().getOutputDirectory());

            final List<URL> urls = new ArrayList<>();
            for (int i = 0; i < classpathElements.size(); ++i) {
                URL url = new File(classpathElements.get(i)).toURI().toURL();
                getLog().debug("Classpath: " + url);
                urls.add(url);
            }

            Set<Artifact> artifacts = this.project.getDependencyArtifacts();
            for (Artifact artifact : artifacts) {
                if (!Artifact.SCOPE_TEST.equalsIgnoreCase(artifact.getScope())) {
                    URL url = artifact.getFile().toURI().toURL();
                    urls.add(url);
                    getLog().debug("Classpath: " + url);
                }
            }

            URL tmp[] = urls.toArray(new URL[urls.size()]);
            return new URLClassLoader(tmp, delegate);
        } catch (final Exception e) {
            getLog().debug("Couldn't get the classloader.");
            return this.getClass().getClassLoader();
        }
    }
}
