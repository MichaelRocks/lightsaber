/*
 * Copyright 2015 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.michaelrocks.bintray

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.component.Artifact
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

class BintrayPlugin implements Plugin<Project> {
    private static final PROVIDED_CONFIGURATION_NAME = "provided"

    private Project project
    private Logger logger

    @Override
    void apply(final Project project) {
        this.project = project
        this.logger = project.logger

        project.apply plugin: 'java'
        project.apply plugin: 'maven-publish'
        project.apply plugin: 'com.jfrog.bintray'
        project.apply plugin: 'idea'

        addProvidedConfiguration()
        configureIdeaModule()

        project.afterEvaluate {
            configureBintrayPublishing()
            modifyPomDependencyScopes()
        }
    }

    private void addProvidedConfiguration() {
        final Configuration provided = addConfiguration(project.configurations, PROVIDED_CONFIGURATION_NAME)
        final Javadoc javadoc = project.tasks.getByName(JavaPlugin.JAVADOC_TASK_NAME) as Javadoc
        javadoc.classpath = javadoc.classpath.plus(provided)
    }

    private static Configuration addConfiguration(final ConfigurationContainer configurations, final String name) {
        final Configuration compile = configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME)
        final Configuration configuration = configurations.create(name)

        compile.extendsFrom(configuration)
        configuration.visible = false
        configuration.transitive = false

        configuration.allDependencies.all { final dependency ->
            configurations.default.exclude(group: dependency.group, module: dependency.name)
        }

        return configuration
    }

    private void configureIdeaModule() {
        project.idea.module {
            scopes.PROVIDED.plus += [project.configurations.provided]
        }
    }

    private void modifyPomDependencyScopes() {
        modifyPomDependencyScope(JavaPlugin.COMPILE_CONFIGURATION_NAME)
        modifyPomDependencyScope(PROVIDED_CONFIGURATION_NAME)
    }

    private void modifyPomDependencyScope(final String scope) {
        project.publishing.publications.all {
            pom.withXml {
                final DependencySet dependencies = project.configurations.getByName(scope).allDependencies
                asNode().dependencies.'*'.findAll() { isDependencyInScope(it, dependencies) }
                        .each { it.scope*.value = scope }
            }
        }
    }

    private boolean isDependencyInScope(final Node node, final DependencySet dependencies) {
        final String groupId = node.groupId.text()
        final String artifactId = node.artifactId.text()
        final String version = node.version.text()
        return dependencies.find { dependency ->
            if (dependency instanceof ProjectDependency) {
                project.rootProject.name + '-' + dependency.name == artifactId
            } else {
                dependency.group == groupId && dependency.name == artifactId && dependency.version == version
            }
        }
    }

    private void configureBintrayPublishing() {
        final boolean hasCredentials = project.hasProperty('bintrayUser') && project.hasProperty('bintrayKey')
        if (hasCredentials) {
            addBintrayRepository()
            configureBintray()
        }
    }

    private void addBintrayRepository() {
        project.publishing {
            repositories {
                maven {
                    url 'https://dl.bintray.com/michaelrocks/lightsaber'
                    if (hasCredentials) {
                        credentials {
                            username = project.property('bintrayUser')
                            password = project.property('bintrayKey')
                        }
                    }
                }
            }
        }
    }

    private void configureBintray() {
        project.bintray {
            user = project.property('bintrayUser')
            key = project.property('bintrayKey')

            publications = ['mavenJava']

            dryRun = project.dryRun
            publish = project.publish
            pkg {
                repo = 'lightsaber'
                name = project.rootProject.name + '-' + project.name
                websiteUrl = 'https://github.com/michaelrocks/lightsaber'
                issueTrackerUrl = 'https://github.com/michaelrocks/lightsaber/issues'
                vcsUrl = 'https://github.com/michaelrocks/lightsaber'
                licenses = ['Apache-2.0']
                labels = ['lightsaber']
                publicDownloadNumbers = true

                version {
                    released = new Date()
                    vcsTag = "v${project.version}"
                }
            }
        }

        project.task('sourcesJar', type: Jar, dependsOn: project.classes) {
            classifier = 'sources'
            from project.sourceSets.main.allSource
        }

        project.task('javadocJar', type: Jar, dependsOn: project.javadoc) {
            classifier = 'javadoc'
            from project.javadoc.destinationDir
        }

        project.artifacts {
            archives project.sourcesJar, project.javadocJar
        }

        project.publishing {
            publications {
                mavenJava(MavenPublication) {
                    artifactId project.bintray.pkg.name
                    if (project.plugins.hasPlugin('war')) {
                        from project.components.web
                    } else {
                        from project.components.java
                    }

                    Artifact sourcesJar
                    Artifact javadocJar

                }
            }
        }
    }
}
