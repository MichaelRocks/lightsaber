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
import org.gradle.api.component.Artifact
import org.gradle.api.logging.Logger
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

class BintrayPlugin implements Plugin<Project> {
    private Project project
    private Logger logger

    @Override
    void apply(final Project project) {
        this.project = project
        this.logger = project.logger

        project.afterEvaluate {
            project.apply plugin: 'maven-publish'
            project.apply plugin: 'com.jfrog.bintray'

            final boolean hasCredentials = project.hasProperty('bintrayUser') && project.hasProperty('bintrayKey')

            project.repositories {
                mavenLocal()
                jcenter()
                mavenCentral()
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

            if (hasCredentials) {
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
    }
}
