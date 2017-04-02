Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T17:41:47+01:00)
Maven home: C:\Dev\Maven\v3.3.9\bin\..
Java version: 1.8.0_112, vendor: Oracle Corporation
Java home: C:\Dev\Java\jdk1.8.0_112\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 7", version: "6.1", arch: "amd64", family: "dos"
[DEBUG] Created new class realm maven.api
[DEBUG] Importing foreign packages into class realm maven.api
[DEBUG]   Imported: javax.enterprise.inject.* < plexus.core
[DEBUG]   Imported: javax.enterprise.util.* < plexus.core
[DEBUG]   Imported: javax.inject.* < plexus.core
[DEBUG]   Imported: org.apache.maven.* < plexus.core
[DEBUG]   Imported: org.apache.maven.artifact < plexus.core
[DEBUG]   Imported: org.apache.maven.classrealm < plexus.core
[DEBUG]   Imported: org.apache.maven.cli < plexus.core
[DEBUG]   Imported: org.apache.maven.configuration < plexus.core
[DEBUG]   Imported: org.apache.maven.exception < plexus.core
[DEBUG]   Imported: org.apache.maven.execution < plexus.core
[DEBUG]   Imported: org.apache.maven.execution.scope < plexus.core
[DEBUG]   Imported: org.apache.maven.lifecycle < plexus.core
[DEBUG]   Imported: org.apache.maven.model < plexus.core
[DEBUG]   Imported: org.apache.maven.monitor < plexus.core
[DEBUG]   Imported: org.apache.maven.plugin < plexus.core
[DEBUG]   Imported: org.apache.maven.profiles < plexus.core
[DEBUG]   Imported: org.apache.maven.project < plexus.core
[DEBUG]   Imported: org.apache.maven.reporting < plexus.core
[DEBUG]   Imported: org.apache.maven.repository < plexus.core
[DEBUG]   Imported: org.apache.maven.rtinfo < plexus.core
[DEBUG]   Imported: org.apache.maven.settings < plexus.core
[DEBUG]   Imported: org.apache.maven.toolchain < plexus.core
[DEBUG]   Imported: org.apache.maven.usability < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.* < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.authentication < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.authorization < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.events < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.observers < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.proxy < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.repository < plexus.core
[DEBUG]   Imported: org.apache.maven.wagon.resource < plexus.core
[DEBUG]   Imported: org.codehaus.classworlds < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.* < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.classworlds < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.component < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.configuration < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.container < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.context < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.lifecycle < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.logging < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.personality < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.util.xml.Xpp3Dom < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.util.xml.pull.XmlPullParser < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.util.xml.pull.XmlPullParserException < plexus.core
[DEBUG]   Imported: org.codehaus.plexus.util.xml.pull.XmlSerializer < plexus.core
[DEBUG]   Imported: org.eclipse.aether.* < plexus.core
[DEBUG]   Imported: org.eclipse.aether.artifact < plexus.core
[DEBUG]   Imported: org.eclipse.aether.collection < plexus.core
[DEBUG]   Imported: org.eclipse.aether.deployment < plexus.core
[DEBUG]   Imported: org.eclipse.aether.graph < plexus.core
[DEBUG]   Imported: org.eclipse.aether.impl < plexus.core
[DEBUG]   Imported: org.eclipse.aether.installation < plexus.core
[DEBUG]   Imported: org.eclipse.aether.internal.impl < plexus.core
[DEBUG]   Imported: org.eclipse.aether.metadata < plexus.core
[DEBUG]   Imported: org.eclipse.aether.repository < plexus.core
[DEBUG]   Imported: org.eclipse.aether.resolution < plexus.core
[DEBUG]   Imported: org.eclipse.aether.spi < plexus.core
[DEBUG]   Imported: org.eclipse.aether.transfer < plexus.core
[DEBUG]   Imported: org.eclipse.aether.version < plexus.core
[DEBUG]   Imported: org.slf4j.* < plexus.core
[DEBUG]   Imported: org.slf4j.helpers.* < plexus.core
[DEBUG]   Imported: org.slf4j.spi.* < plexus.core
[DEBUG] Populating class realm maven.api
[INFO] Error stacktraces are turned on.
[DEBUG] Reading global settings from C:\Dev\Maven\v3.3.9\bin\..\conf\settings.xml
[DEBUG] Reading user settings from C:\Users\krevelvr\.m2\settings.xml
[DEBUG] Reading global toolchains from C:\Dev\Maven\v3.3.9\bin\..\conf\toolchains.xml
[DEBUG] Reading user toolchains from C:\Users\krevelvr\.m2\toolchains.xml
[DEBUG] Using local repository at C:\Dev\Maven\repository
[DEBUG] Using manager EnhancedLocalRepositoryManager with priority 10.0 for C:\Dev\Maven\repository
[INFO] Scanning for projects...
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=0, ConflictMarker.markTime=1, ConflictMarker.nodeCount=10, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=1, ConflictIdSorter.conflictIdCount=6, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=4, ConflictResolver.conflictItemCount=10, DefaultDependencyCollector.collectTime=75, DefaultDependencyCollector.transformTime=9}
[DEBUG] org.apache.maven.wagon:wagon-ssh:jar:1.0-beta-7:
[DEBUG]    com.jcraft:jsch:jar:0.1.38:compile
[DEBUG]    org.codehaus.plexus:plexus-utils:jar:1.4.2:compile
[DEBUG]    org.apache.maven.wagon:wagon-ssh-common:jar:1.0-beta-7:compile
[DEBUG]       org.codehaus.plexus:plexus-interactivity-api:jar:1.0-alpha-6:compile
[DEBUG]    org.apache.maven.wagon:wagon-provider-api:jar:1.0-beta-7:compile
[DEBUG] Created new class realm extension>org.apache.maven.wagon:wagon-ssh:1.0-beta-7
[DEBUG] Importing foreign packages into class realm extension>org.apache.maven.wagon:wagon-ssh:1.0-beta-7
[DEBUG]   Imported:  < maven.api
[DEBUG] Populating class realm extension>org.apache.maven.wagon:wagon-ssh:1.0-beta-7
[DEBUG]   Included: org.apache.maven.wagon:wagon-ssh:jar:1.0-beta-7
[DEBUG]   Included: com.jcraft:jsch:jar:0.1.38
[DEBUG]   Included: org.codehaus.plexus:plexus-utils:jar:1.4.2
[DEBUG]   Included: org.apache.maven.wagon:wagon-ssh-common:jar:1.0-beta-7
[DEBUG]   Included: org.codehaus.plexus:plexus-interactivity-api:jar:1.0-alpha-6
[DEBUG] Extension realms for project nl.rivm.cib:epidemes-demo-webapp:war:0.1.0-SNAPSHOT: [ClassRealm[extension>org.apache.maven.wagon:wagon-ssh:1.0-beta-7, parent: sun.misc.Launcher$AppClassLoader@55f96302]]
[DEBUG] Created new class realm project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Populating class realm project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Looking up lifecyle mappings for packaging war from ClassRealm[project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT, parent: ClassRealm[maven.api, parent: null]]
[DEBUG] Extension realms for project nl.rivm.cib:epidemes:pom:0.1.0-SNAPSHOT: [ClassRealm[extension>org.apache.maven.wagon:wagon-ssh:1.0-beta-7, parent: sun.misc.Launcher$AppClassLoader@55f96302]]
[DEBUG] Looking up lifecyle mappings for packaging pom from ClassRealm[project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT, parent: ClassRealm[maven.api, parent: null]]
[DEBUG] === REACTOR BUILD PLAN ================================================
[DEBUG] Project: nl.rivm.cib:epidemes-demo-webapp:war:0.1.0-SNAPSHOT
[DEBUG] Tasks:   [clean, verify, org.codehaus.cargo:cargo-maven2-plugin:run]
[DEBUG] Style:   Regular
[DEBUG] =======================================================================
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building epidemes-demo-webapp 0.1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] Lifecycle default -> [validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy]
[DEBUG] Lifecycle clean -> [pre-clean, clean, post-clean]
[DEBUG] Lifecycle site -> [pre-site, site, post-site, site-deploy]
[DEBUG] === PROJECT BUILD PLAN ================================================
[DEBUG] Project:       nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Dependencies (collect): []
[DEBUG] Dependencies (resolve): [compile, runtime, test]
[DEBUG] Repositories (dependencies): [central (https://repo.maven.apache.org/maven2, default, releases)]
[DEBUG] Repositories (plugins)     : [central (https://repo.maven.apache.org/maven2, default, releases)]
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.apache.maven.plugins:maven-clean-plugin:3.0.0:clean (default-clean)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <directory default-value="${project.build.directory}"/>
  <excludeDefaultDirectories default-value="false">${maven.clean.excludeDefaultDirectories}</excludeDefaultDirectories>
  <failOnError default-value="true">${maven.clean.failOnError}</failOnError>
  <followSymLinks default-value="false">${maven.clean.followSymLinks}</followSymLinks>
  <outputDirectory default-value="${project.build.outputDirectory}"/>
  <reportDirectory default-value="${project.build.outputDirectory}"/>
  <retryOnError default-value="true">${maven.clean.retryOnError}</retryOnError>
  <skip default-value="false">${maven.clean.skip}</skip>
  <testOutputDirectory default-value="${project.build.testOutputDirectory}"/>
  <verbose>${maven.clean.verbose}</verbose>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:prepare-agent (default-prepare-agent)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <address>${jacoco.address}</address>
  <append>${jacoco.append}</append>
  <classDumpDir>${jacoco.classDumpDir}</classDumpDir>
  <destFile default-value="${project.build.directory}/jacoco.exec">${jacoco.destFile}</destFile>
  <dumpOnExit>${jacoco.dumpOnExit}</dumpOnExit>
  <exclClassLoaders>${jacoco.exclClassLoaders}</exclClassLoaders>
  <inclBootstrapClasses>${jacoco.inclBootstrapClasses}</inclBootstrapClasses>
  <inclNoLocationClasses>${jacoco.inclNoLocationClasses}</inclNoLocationClasses>
  <jmx>${jacoco.jmx}</jmx>
  <output>${jacoco.output}</output>
  <pluginArtifactMap>${plugin.artifactMap}</pluginArtifactMap>
  <port>${jacoco.port}</port>
  <project>${project}</project>
  <propertyName>${jacoco.propertyName}</propertyName>
  <sessionId>${jacoco.sessionId}</sessionId>
  <skip default-value="false">${jacoco.skip}</skip>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.apache.maven.plugins:maven-resources-plugin:3.0.1:resources (default-resources)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <addDefaultExcludes default-value="true"/>
  <buildFilters default-value="${project.build.filters}"/>
  <encoding default-value="${project.build.sourceEncoding}">UTF-8</encoding>
  <escapeString default-value="\"/>
  <escapeWindowsPaths default-value="true"/>
  <fileNameFiltering default-value="false"/>
  <includeEmptyDirs default-value="false"/>
  <outputDirectory default-value="${project.build.outputDirectory}"/>
  <overwrite default-value="false"/>
  <project default-value="${project}"/>
  <resources default-value="${project.resources}"/>
  <session default-value="${session}"/>
  <skip default-value="false">${maven.resources.skip}</skip>
  <supportMultiLineFiltering default-value="false"/>
  <useBuildFilters default-value="true"/>
  <useDefaultDelimiters default-value="true"/>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.apache.maven.plugins:maven-compiler-plugin:3.5.1:compile (default-compile)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <basedir default-value="${basedir}"/>
  <buildDirectory default-value="${project.build.directory}"/>
  <classpathElements default-value="${project.compileClasspathElements}"/>
  <compileSourceRoots default-value="${project.compileSourceRoots}"/>
  <compilerArgument>-Xlint:unchecked</compilerArgument>
  <compilerArguments>
    <Xmaxerrs>1000</Xmaxerrs>
    <Averbose>true</Averbose>
    <Xlint/>
  </compilerArguments>
  <compilerId default-value="javac">${maven.compiler.compilerId}</compilerId>
  <compilerReuseStrategy default-value="${reuseCreated}">${maven.compiler.compilerReuseStrategy}</compilerReuseStrategy>
  <compilerVersion>${maven.compiler.compilerVersion}</compilerVersion>
  <debug default-value="true">${maven.compiler.debug}</debug>
  <debuglevel>${maven.compiler.debuglevel}</debuglevel>
  <encoding default-value="${project.build.sourceEncoding}">UTF-8</encoding>
  <executable>${maven.compiler.executable}</executable>
  <failOnError default-value="true">${maven.compiler.failOnError}</failOnError>
  <forceJavacCompilerUse default-value="false">${maven.compiler.forceJavacCompilerUse}</forceJavacCompilerUse>
  <fork default-value="false">${maven.compiler.fork}</fork>
  <generatedSourcesDirectory default-value="${project.build.directory}/generated-sources/annotations"/>
  <maxmem>${maven.compiler.maxmem}</maxmem>
  <meminitial>${maven.compiler.meminitial}</meminitial>
  <mojoExecution default-value="${mojoExecution}"/>
  <optimize default-value="false">${maven.compiler.optimize}</optimize>
  <outputDirectory default-value="${project.build.outputDirectory}"/>
  <project default-value="${project}"/>
  <projectArtifact default-value="${project.artifact}"/>
  <session default-value="${session}"/>
  <showDeprecation default-value="false">${maven.compiler.showDeprecation}</showDeprecation>
  <showWarnings default-value="false">${maven.compiler.showWarnings}</showWarnings>
  <skipMain>${maven.main.skip}</skipMain>
  <skipMultiThreadWarning default-value="false">${maven.compiler.skipMultiThreadWarning}</skipMultiThreadWarning>
  <source default-value="1.5">1.8</source>
  <staleMillis default-value="0">${lastModGranularityMs}</staleMillis>
  <target default-value="1.5">1.8</target>
  <useIncrementalCompilation default-value="true">${maven.compiler.useIncrementalCompilation}</useIncrementalCompilation>
  <verbose default-value="false">false</verbose>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.apache.maven.plugins:maven-resources-plugin:3.0.1:testResources (default-testResources)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <addDefaultExcludes default-value="true"/>
  <buildFilters default-value="${project.build.filters}"/>
  <encoding default-value="${project.build.sourceEncoding}">UTF-8</encoding>
  <escapeString default-value="\"/>
  <escapeWindowsPaths default-value="true"/>
  <fileNameFiltering default-value="false"/>
  <includeEmptyDirs default-value="false"/>
  <outputDirectory default-value="${project.build.testOutputDirectory}"/>
  <overwrite default-value="false"/>
  <project default-value="${project}"/>
  <resources default-value="${project.testResources}"/>
  <session default-value="${session}"/>
  <skip default-value="false">${maven.test.skip}</skip>
  <supportMultiLineFiltering default-value="false"/>
  <useBuildFilters default-value="true"/>
  <useDefaultDelimiters default-value="true"/>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.apache.maven.plugins:maven-compiler-plugin:3.5.1:testCompile (default-testCompile)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <basedir default-value="${basedir}"/>
  <buildDirectory default-value="${project.build.directory}"/>
  <classpathElements default-value="${project.testClasspathElements}"/>
  <compileSourceRoots default-value="${project.testCompileSourceRoots}"/>
  <compilerArgument>-Xlint:unchecked</compilerArgument>
  <compilerArguments>
    <Xmaxerrs>1000</Xmaxerrs>
    <Averbose>true</Averbose>
    <Xlint/>
  </compilerArguments>
  <compilerId default-value="javac">${maven.compiler.compilerId}</compilerId>
  <compilerReuseStrategy default-value="${reuseCreated}">${maven.compiler.compilerReuseStrategy}</compilerReuseStrategy>
  <compilerVersion>${maven.compiler.compilerVersion}</compilerVersion>
  <debug default-value="true">${maven.compiler.debug}</debug>
  <debuglevel>${maven.compiler.debuglevel}</debuglevel>
  <encoding default-value="${project.build.sourceEncoding}">UTF-8</encoding>
  <executable>${maven.compiler.executable}</executable>
  <failOnError default-value="true">${maven.compiler.failOnError}</failOnError>
  <forceJavacCompilerUse default-value="false">${maven.compiler.forceJavacCompilerUse}</forceJavacCompilerUse>
  <fork default-value="false">${maven.compiler.fork}</fork>
  <generatedTestSourcesDirectory default-value="${project.build.directory}/generated-test-sources/test-annotations"/>
  <maxmem>${maven.compiler.maxmem}</maxmem>
  <meminitial>${maven.compiler.meminitial}</meminitial>
  <mojoExecution default-value="${mojoExecution}"/>
  <optimize default-value="false">${maven.compiler.optimize}</optimize>
  <outputDirectory default-value="${project.build.testOutputDirectory}"/>
  <project default-value="${project}"/>
  <session default-value="${session}"/>
  <showDeprecation default-value="false">${maven.compiler.showDeprecation}</showDeprecation>
  <showWarnings default-value="false">${maven.compiler.showWarnings}</showWarnings>
  <skip>${maven.test.skip}</skip>
  <skipMultiThreadWarning default-value="false">${maven.compiler.skipMultiThreadWarning}</skipMultiThreadWarning>
  <source default-value="1.5">1.8</source>
  <staleMillis default-value="0">${lastModGranularityMs}</staleMillis>
  <target default-value="1.5">1.8</target>
  <testSource>${maven.compiler.testSource}</testSource>
  <testTarget>${maven.compiler.testTarget}</testTarget>
  <useIncrementalCompilation default-value="true">${maven.compiler.useIncrementalCompilation}</useIncrementalCompilation>
  <verbose default-value="false">false</verbose>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.apache.maven.plugins:maven-surefire-plugin:2.19.1:test (default-test)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <additionalClasspathElements>${maven.test.additionalClasspath}</additionalClasspathElements>
  <argLine>${argLine}</argLine>
  <basedir default-value="${basedir}"/>
  <childDelegation default-value="false">${childDelegation}</childDelegation>
  <classesDirectory default-value="${project.build.outputDirectory}"/>
  <classpathDependencyExcludes>${maven.test.dependency.excludes}</classpathDependencyExcludes>
  <debugForkedProcess>${maven.surefire.debug}</debugForkedProcess>
  <dependenciesToScan>${dependenciesToScan}</dependenciesToScan>
  <disableXmlReport default-value="false">${disableXmlReport}</disableXmlReport>
  <enableAssertions default-value="true">${enableAssertions}</enableAssertions>
  <excludedGroups>${excludedGroups}</excludedGroups>
  <excludesFile>${surefire.excludesFile}</excludesFile>
  <failIfNoSpecifiedTests>${surefire.failIfNoSpecifiedTests}</failIfNoSpecifiedTests>
  <failIfNoTests>${failIfNoTests}</failIfNoTests>
  <forkCount default-value="1">${forkCount}</forkCount>
  <forkMode default-value="once">${forkMode}</forkMode>
  <forkedProcessTimeoutInSeconds>${surefire.timeout}</forkedProcessTimeoutInSeconds>
  <groups>${groups}</groups>
  <includesFile>${surefire.includesFile}</includesFile>
  <junitArtifactName default-value="junit:junit">${junitArtifactName}</junitArtifactName>
  <jvm>${jvm}</jvm>
  <localRepository default-value="${localRepository}"/>
  <objectFactory>${objectFactory}</objectFactory>
  <parallel>methods</parallel>
  <parallelMavenExecution default-value="${session.parallel}"/>
  <parallelOptimized default-value="true">${parallelOptimized}</parallelOptimized>
  <parallelTestsTimeoutForcedInSeconds>${surefire.parallel.forcedTimeout}</parallelTestsTimeoutForcedInSeconds>
  <parallelTestsTimeoutInSeconds>${surefire.parallel.timeout}</parallelTestsTimeoutInSeconds>
  <perCoreThreadCount default-value="true">${perCoreThreadCount}</perCoreThreadCount>
  <pluginArtifactMap>${plugin.artifactMap}</pluginArtifactMap>
  <pluginDescriptor default-value="${plugin}"/>
  <printSummary default-value="true">${surefire.printSummary}</printSummary>
  <projectArtifactMap>${project.artifactMap}</projectArtifactMap>
  <redirectTestOutputToFile default-value="false">${maven.test.redirectTestOutputToFile}</redirectTestOutputToFile>
  <remoteRepositories default-value="${project.pluginArtifactRepositories}"/>
  <reportFormat default-value="brief">${surefire.reportFormat}</reportFormat>
  <reportNameSuffix default-value="">${surefire.reportNameSuffix}</reportNameSuffix>
  <reportsDirectory default-value="${project.build.directory}/surefire-reports"/>
  <rerunFailingTestsCount default-value="0">${surefire.rerunFailingTestsCount}</rerunFailingTestsCount>
  <reuseForks default-value="true">${reuseForks}</reuseForks>
  <runOrder default-value="filesystem">${surefire.runOrder}</runOrder>
  <shutdown default-value="testset">${surefire.shutdown}</shutdown>
  <skip default-value="false">${maven.test.skip}</skip>
  <skipAfterFailureCount default-value="0">${surefire.skipAfterFailureCount}</skipAfterFailureCount>
  <skipExec>${maven.test.skip.exec}</skipExec>
  <skipTests default-value="false">false</skipTests>
  <suiteXmlFiles>${surefire.suiteXmlFiles}</suiteXmlFiles>
  <test>${test}</test>
  <testClassesDirectory default-value="${project.build.testOutputDirectory}"/>
  <testFailureIgnore default-value="false">${maven.test.failure.ignore}</testFailureIgnore>
  <testNGArtifactName default-value="org.testng:testng">${testNGArtifactName}</testNGArtifactName>
  <testSourceDirectory default-value="${project.build.testSourceDirectory}"/>
  <threadCount>1</threadCount>
  <threadCountClasses default-value="0">${threadCountClasses}</threadCountClasses>
  <threadCountMethods default-value="0">${threadCountMethods}</threadCountMethods>
  <threadCountSuites default-value="0">${threadCountSuites}</threadCountSuites>
  <trimStackTrace default-value="true">${trimStackTrace}</trimStackTrace>
  <useFile default-value="true">${surefire.useFile}</useFile>
  <useManifestOnlyJar default-value="true">${surefire.useManifestOnlyJar}</useManifestOnlyJar>
  <useSystemClassLoader default-value="true">${surefire.useSystemClassLoader}</useSystemClassLoader>
  <useUnlimitedThreads default-value="false">${useUnlimitedThreads}</useUnlimitedThreads>
  <workingDirectory>${basedir}</workingDirectory>
  <project default-value="${project}"/>
  <session default-value="${session}"/>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:report (default-report)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <dataFile default-value="${project.build.directory}/jacoco.exec"/>
  <outputDirectory default-value="${project.reporting.outputDirectory}/jacoco"/>
  <outputEncoding default-value="UTF-8">${project.reporting.outputEncoding}</outputEncoding>
  <project>${project}</project>
  <skip default-value="false">${jacoco.skip}</skip>
  <sourceEncoding default-value="UTF-8">${project.build.sourceEncoding}</sourceEncoding>
  <title default-value="${project.name}"/>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.apache.maven.plugins:maven-war-plugin:2.6:war (default-war)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <archiveClasses default-value="false">${archiveClasses}</archiveClasses>
  <attachClasses default-value="false"/>
  <cacheFile default-value="${project.build.directory}/war/work/webapp-cache.xml"/>
  <classesClassifier default-value="classes"/>
  <classesDirectory default-value="${project.build.outputDirectory}"/>
  <containerConfigXML>${maven.war.containerConfigXML}</containerConfigXML>
  <escapeString>${maven.war.escapeString}</escapeString>
  <escapedBackslashesInFilePath default-value="false">${maven.war.escapedBackslashesInFilePath}</escapedBackslashesInFilePath>
  <failOnMissingWebXml default-value="true">${failOnMissingWebXml}</failOnMissingWebXml>
  <filteringDeploymentDescriptors default-value="false">${maven.war.filteringDeploymentDescriptors}</filteringDeploymentDescriptors>
  <includeEmptyDirectories default-value="false"/>
  <outputDirectory default-value="${project.build.directory}"/>
  <primaryArtifact default-value="true">${primaryArtifact}</primaryArtifact>
  <project default-value="${project}"/>
  <recompressZippedFiles default-value="true"/>
  <resourceEncoding default-value="${project.build.sourceEncoding}">${resourceEncoding}</resourceEncoding>
  <session default-value="${session}"/>
  <supportMultiLineFiltering default-value="false">${maven.war.supportMultiLineFiltering}</supportMultiLineFiltering>
  <useCache default-value="false">${useCache}</useCache>
  <useJvmChmod default-value="true">${maven.war.useJvmChmod}</useJvmChmod>
  <warName default-value="${project.build.finalName}">${war.warName}</warName>
  <warSourceDirectory default-value="${basedir}/src/main/webapp"/>
  <warSourceIncludes default-value="**"/>
  <webXml>${maven.war.webxml}</webXml>
  <webappDirectory default-value="${project.build.directory}/${project.build.finalName}"/>
  <workDirectory default-value="${project.build.directory}/war/work"/>
</configuration>
[DEBUG] --- init fork of nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT for org.apache.maven.plugins:maven-source-plugin:3.0.0:jar (attach-sources) ---
[DEBUG] Dependencies (collect): []
[DEBUG] Dependencies (resolve): [runtime]
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:prepare-agent (default-prepare-agent)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <address>${jacoco.address}</address>
  <append>${jacoco.append}</append>
  <classDumpDir>${jacoco.classDumpDir}</classDumpDir>
  <destFile default-value="${project.build.directory}/jacoco.exec">${jacoco.destFile}</destFile>
  <dumpOnExit>${jacoco.dumpOnExit}</dumpOnExit>
  <exclClassLoaders>${jacoco.exclClassLoaders}</exclClassLoaders>
  <inclBootstrapClasses>${jacoco.inclBootstrapClasses}</inclBootstrapClasses>
  <inclNoLocationClasses>${jacoco.inclNoLocationClasses}</inclNoLocationClasses>
  <jmx>${jacoco.jmx}</jmx>
  <output>${jacoco.output}</output>
  <pluginArtifactMap>${plugin.artifactMap}</pluginArtifactMap>
  <port>${jacoco.port}</port>
  <project>${project}</project>
  <propertyName>${jacoco.propertyName}</propertyName>
  <sessionId>${jacoco.sessionId}</sessionId>
  <skip default-value="false">${jacoco.skip}</skip>
</configuration>
[DEBUG] --- exit fork of nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT for org.apache.maven.plugins:maven-source-plugin:3.0.0:jar (attach-sources) ---
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.apache.maven.plugins:maven-source-plugin:3.0.0:jar (attach-sources)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <attach default-value="true">${maven.source.attach}</attach>
  <classifier default-value="sources">${maven.source.classifier}</classifier>
  <defaultManifestFile default-value="${project.build.outputDirectory}/META-INF/MANIFEST.MF"/>
  <excludeResources default-value="false">${maven.source.excludeResources}</excludeResources>
  <finalName default-value="${project.build.finalName}"/>
  <forceCreation default-value="false">${maven.source.forceCreation}</forceCreation>
  <includePom default-value="false">${maven.source.includePom}</includePom>
  <outputDirectory default-value="${project.build.directory}"/>
  <project default-value="${project}"/>
  <reactorProjects default-value="${reactorProjects}"/>
  <session default-value="${session}"/>
  <skipSource default-value="false">${maven.source.skip}</skipSource>
  <useDefaultExcludes default-value="true">${maven.source.useDefaultExcludes}</useDefaultExcludes>
  <useDefaultManifestFile default-value="false">${maven.source.useDefaultManifestFile}</useDefaultManifestFile>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.codehaus.cargo:cargo-maven2-plugin:1.6.2:start (start-cargo)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <container>
    <containerId>wildfly10x</containerId>
    <zipUrlInstaller>
      <url>http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip</url>
      <downloadDir>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.cargo/downloads</downloadDir>
      <extractDir>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.cargo/extracts</extractDir>
    </zipUrlInstaller>
    <log>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target/cargo.log</log>
    <output>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target/wildfly.log</output>
    <systemProperties></systemProperties>
  </container>
  <deployables>
    <deployable>
      <properties>
        <context>epidemes</context>
      </properties>
      <pingTimeout>120000</pingTimeout>
    </deployable>
  </deployables>
  <ignoreFailures default-value="false">${cargo.ignore.failures}</ignoreFailures>
  <localRepository>${localRepository}</localRepository>
  <pluginVersion>${plugin.version}</pluginVersion>
  <project>${project}</project>
  <repositories>${project.remoteArtifactRepositories}</repositories>
  <settings>${settings}</settings>
  <skip default-value="false">${cargo.maven.skip}</skip>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.codehaus.cargo:cargo-maven2-plugin:1.6.2:stop (stop-cargo)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <container>
    <containerId>wildfly10x</containerId>
    <zipUrlInstaller>
      <url>http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip</url>
      <downloadDir>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.cargo/downloads</downloadDir>
      <extractDir>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.cargo/extracts</extractDir>
    </zipUrlInstaller>
    <log>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target/cargo.log</log>
    <output>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target/wildfly.log</output>
    <systemProperties></systemProperties>
  </container>
  <deployables>
    <deployable>
      <properties>
        <context>epidemes</context>
      </properties>
      <pingTimeout>120000</pingTimeout>
    </deployable>
  </deployables>
  <ignoreFailures default-value="false">${cargo.ignore.failures}</ignoreFailures>
  <localRepository>${localRepository}</localRepository>
  <pluginVersion>${plugin.version}</pluginVersion>
  <project>${project}</project>
  <repositories>${project.remoteArtifactRepositories}</repositories>
  <settings>${settings}</settings>
  <skip default-value="false">${cargo.maven.skip}</skip>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:check (default-check)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <dataFile default-value="${project.build.directory}/jacoco.exec"/>
  <haltOnFailure default-value="true">${jacoco.haltOnFailure}</haltOnFailure>
  <project>${project}</project>
  <rules>
    <rule>
      <element>BUNDLE</element>
      <limits>
        <limit>
          <counter>COMPLEXITY</counter>
          <value>COVEREDRATIO</value>
          <minimum>0.0</minimum>
        </limit>
      </limits>
    </rule>
  </rules>
  <skip default-value="false">${jacoco.skip}</skip>
</configuration>
[DEBUG] -----------------------------------------------------------------------
[DEBUG] Goal:          org.codehaus.cargo:cargo-maven2-plugin:1.6.2:run (default-cli)
[DEBUG] Style:         Regular
[DEBUG] Configuration: <?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <container>
    <containerId>wildfly10x</containerId>
    <zipUrlInstaller>
      <url>http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip</url>
      <downloadDir>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.cargo/downloads</downloadDir>
      <extractDir>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.cargo/extracts</extractDir>
    </zipUrlInstaller>
    <log>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target/cargo.log</log>
    <output>C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target/wildfly.log</output>
    <systemProperties></systemProperties>
  </container>
  <containerId>${cargo.maven.containerId}</containerId>
  <containerUrl>${cargo.maven.containerUrl}</containerUrl>
  <deployables>
    <deployable>
      <properties>
        <context>epidemes</context>
      </properties>
      <pingTimeout>120000</pingTimeout>
    </deployable>
  </deployables>
  <ignoreFailures default-value="false">${cargo.ignore.failures}</ignoreFailures>
  <localRepository>${localRepository}</localRepository>
  <pluginVersion>${plugin.version}</pluginVersion>
  <project>${project}</project>
  <repositories>${project.remoteArtifactRepositories}</repositories>
  <settings>${settings}</settings>
  <skip default-value="false">${cargo.maven.skip}</skip>
</configuration>
[DEBUG] =======================================================================
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=0, ConflictMarker.markTime=0, ConflictMarker.nodeCount=33, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=0, ConflictIdSorter.conflictIdCount=25, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=3, ConflictResolver.conflictItemCount=32, DefaultDependencyCollector.collectTime=142, DefaultDependencyCollector.transformTime=3}
[DEBUG] nl.rivm.cib:epidemes-demo-webapp:war:0.1.0-SNAPSHOT
[DEBUG]    org.codehaus.cargo:cargo-maven2-plugin:jar:1.6.2:compile
[DEBUG]       org.apache.maven:maven-archiver:jar:2.4.1:compile
[DEBUG]          org.apache.maven:maven-artifact:jar:2.0.6:compile
[DEBUG]          org.apache.maven:maven-model:jar:2.0.6:compile
[DEBUG]          org.codehaus.plexus:plexus-archiver:jar:1.0:compile
[DEBUG]             org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile
[DEBUG]                junit:junit:jar:4.11:compile (version managed from 3.8.1 by nl.rivm.cib:epidemes:0.1.0-SNAPSHOT)
[DEBUG]                   org.hamcrest:hamcrest-core:jar:1.3:compile
[DEBUG]                classworlds:classworlds:jar:1.1-alpha-2:compile
[DEBUG]             org.codehaus.plexus:plexus-io:jar:1.0:compile
[DEBUG]          org.codehaus.plexus:plexus-utils:jar:2.0.5:compile
[DEBUG]          org.codehaus.plexus:plexus-interpolation:jar:1.13:compile
[DEBUG]       org.codehaus.cargo:cargo-core-api-generic:jar:1.6.2:compile
[DEBUG]          commons-discovery:commons-discovery:jar:0.5:compile
[DEBUG]             commons-logging:commons-logging:jar:1.1.1:compile
[DEBUG]          org.codehaus.cargo:cargo-core-api-container:jar:1.6.2:compile
[DEBUG]             org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:compile
[DEBUG]                jaxen:jaxen:jar:1.1.6:compile
[DEBUG]                org.jdom:jdom:jar:1.1.3:compile
[DEBUG]             org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:compile
[DEBUG]       org.codehaus.cargo:cargo-documentation:jar:1.6.2:compile
[DEBUG]       org.codehaus.cargo:cargo-daemon-client:jar:1.6.2:compile
[DEBUG]          org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:compile
[DEBUG]             org.apache.ant:ant:jar:1.7.1:compile
[DEBUG]                org.apache.ant:ant-launcher:jar:1.7.1:compile
[INFO] 
[INFO] --- maven-clean-plugin:3.0.0:clean (default-clean) @ epidemes-demo-webapp ---
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=0, ConflictMarker.markTime=0, ConflictMarker.nodeCount=15, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=0, ConflictIdSorter.conflictIdCount=13, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=0, ConflictResolver.conflictItemCount=15, DefaultDependencyCollector.collectTime=82, DefaultDependencyCollector.transformTime=0}
[DEBUG] org.apache.maven.plugins:maven-clean-plugin:jar:3.0.0:
[DEBUG]    org.apache.maven:maven-plugin-api:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-model:jar:3.0:compile
[DEBUG]          org.codehaus.plexus:plexus-utils:jar:2.0.4:compile
[DEBUG]       org.apache.maven:maven-artifact:jar:3.0:compile
[DEBUG]       org.sonatype.sisu:sisu-inject-plexus:jar:1.4.2:compile
[DEBUG]          org.codehaus.plexus:plexus-component-annotations:jar:1.5.5:compile
[DEBUG]          org.codehaus.plexus:plexus-classworlds:jar:2.2.3:compile
[DEBUG]          org.sonatype.sisu:sisu-inject-bean:jar:1.4.2:compile
[DEBUG]             org.sonatype.sisu:sisu-guice:jar:noaop:2.1.7:compile
[DEBUG]    org.apache.maven.shared:maven-shared-utils:jar:3.0.0:compile
[DEBUG]       commons-io:commons-io:jar:2.4:compile
[DEBUG]       com.google.code.findbugs:jsr305:jar:2.0.1:compile
[DEBUG] Created new class realm plugin>org.apache.maven.plugins:maven-clean-plugin:3.0.0
[DEBUG] Importing foreign packages into class realm plugin>org.apache.maven.plugins:maven-clean-plugin:3.0.0
[DEBUG]   Imported:  < project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Populating class realm plugin>org.apache.maven.plugins:maven-clean-plugin:3.0.0
[DEBUG]   Included: org.apache.maven.plugins:maven-clean-plugin:jar:3.0.0
[DEBUG]   Included: org.codehaus.plexus:plexus-utils:jar:2.0.4
[DEBUG]   Included: org.codehaus.plexus:plexus-component-annotations:jar:1.5.5
[DEBUG]   Included: org.sonatype.sisu:sisu-inject-bean:jar:1.4.2
[DEBUG]   Included: org.sonatype.sisu:sisu-guice:jar:noaop:2.1.7
[DEBUG]   Included: org.apache.maven.shared:maven-shared-utils:jar:3.0.0
[DEBUG]   Included: commons-io:commons-io:jar:2.4
[DEBUG]   Included: com.google.code.findbugs:jsr305:jar:2.0.1
[DEBUG] Configuring mojo org.apache.maven.plugins:maven-clean-plugin:3.0.0:clean from plugin realm ClassRealm[plugin>org.apache.maven.plugins:maven-clean-plugin:3.0.0, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.apache.maven.plugins:maven-clean-plugin:3.0.0:clean' with basic configurator -->
[DEBUG]   (f) directory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target
[DEBUG]   (f) excludeDefaultDirectories = false
[DEBUG]   (f) failOnError = true
[DEBUG]   (f) followSymLinks = false
[DEBUG]   (f) outputDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[DEBUG]   (f) reportDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[DEBUG]   (f) retryOnError = true
[DEBUG]   (f) skip = false
[DEBUG]   (f) testOutputDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\test-classes
[DEBUG] -- end configuration --
[INFO] Deleting C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\wildfly.log
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\test-classes
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-status\maven-compiler-plugin\testCompile\default-testCompile\inputFiles.lst
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-status\maven-compiler-plugin\testCompile\default-testCompile
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-status\maven-compiler-plugin\testCompile
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-status\maven-compiler-plugin\compile\default-compile\inputFiles.lst
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-status\maven-compiler-plugin\compile\default-compile
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-status\maven-compiler-plugin\compile
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-status\maven-compiler-plugin
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-status
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-archiver\pom.properties
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\maven-archiver
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT.war
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\web.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\plexus-utils-2.0.5.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\plexus-io-1.0.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\plexus-interpolation-1.13.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\plexus-container-default-1.0-alpha-9-stable-1.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\plexus-archiver-1.0.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\maven-model-2.0.6.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\maven-artifact-2.0.6.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\maven-archiver-2.4.1.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\junit-4.11.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\jdom-1.1.3.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\jaxen-1.1.6.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\hamcrest-core-1.3.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\geronimo-j2ee-deployment_1.1_spec-1.1.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\commons-logging-1.1.1.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\commons-discovery-0.5.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\classworlds-1.1-alpha-2.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\cargo-maven2-plugin-1.6.2.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\cargo-documentation-1.6.2.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\cargo-daemon-client-1.6.2.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\cargo-core-api-util-1.6.2.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\cargo-core-api-module-1.6.2.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\cargo-core-api-generic-1.6.2.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\cargo-core-api-container-1.6.2.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\ant-launcher-1.7.1.jar
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib\ant-1.7.1.jar
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\lib
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF\classes
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\WEB-INF
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\META-INF
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT\index.html
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo.log
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\tmp\vfs\temp
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\tmp\vfs\deployment\deployment3ffe204b384bfcb1
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\tmp\vfs\deployment
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\tmp\vfs
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\tmp\auth
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\tmp
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\log\server.log
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\log\boot.log
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\log
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\lib\ext
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\lib
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\deployments\README.txt
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\deployments\epidemes.war.deployed
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\deployments\epidemes.war
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\deployments
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\data\tx-object-store\ShadowNoFileLockStore\defaultStore
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\data\tx-object-store\ShadowNoFileLockStore
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\data\tx-object-store
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\data\timer-service-data
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\data\kernel\process-uuid
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\data\kernel
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\data
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\content\7a\e8586c51f2abf0e2e22dc31631ed55e37eebc3\content
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\content\7a\e8586c51f2abf0e2e22dc31631ed55e37eebc3
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\content\7a
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\content
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\standalone.last.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\standalone.initial.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\standalone.boot.xml
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\snapshot
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\current
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\20170330-152613445\standalone.v5.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\20170330-152613445\standalone.v4.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\20170330-152613445\standalone.v3.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\20170330-152613445\standalone.v2.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\20170330-152613445\standalone.v1.xml
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history\20170330-152613445
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone_xml_history
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone-ha.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone-full.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\standalone-full-ha.xml
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\mgmt-users.properties
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\mgmt-groups.properties
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\logging.properties
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\application-users.properties
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration\application-roles.properties
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\configuration
[INFO] Deleting file C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x\.cargo
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations\wildfly10x
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo\configurations
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo
[INFO] Deleting directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target
[DEBUG] Skipping non-existing directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[DEBUG] Skipping non-existing directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\test-classes
[DEBUG] Skipping non-existing directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[INFO] 
[INFO] --- jacoco-maven-plugin:0.7.7.201606060606:prepare-agent (default-prepare-agent) @ epidemes-demo-webapp ---
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=1, ConflictMarker.markTime=0, ConflictMarker.nodeCount=158, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=1, ConflictIdSorter.conflictIdCount=47, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=4, ConflictResolver.conflictItemCount=108, DefaultDependencyCollector.collectTime=326, DefaultDependencyCollector.transformTime=6}
[DEBUG] org.jacoco:jacoco-maven-plugin:jar:0.7.7.201606060606:
[DEBUG]    org.apache.maven:maven-plugin-api:jar:2.2.1:compile
[DEBUG]    org.apache.maven:maven-project:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-settings:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-profile:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-model:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-artifact-manager:jar:2.2.1:compile
[DEBUG]          org.apache.maven:maven-repository-metadata:jar:2.2.1:compile
[DEBUG]          backport-util-concurrent:backport-util-concurrent:jar:3.1:compile
[DEBUG]       org.apache.maven:maven-plugin-registry:jar:2.2.1:compile
[DEBUG]       org.codehaus.plexus:plexus-interpolation:jar:1.11:compile
[DEBUG]       org.apache.maven:maven-artifact:jar:2.2.1:compile
[DEBUG]       org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile
[DEBUG]          junit:junit:jar:4.8.2:compile
[DEBUG]          classworlds:classworlds:jar:1.1-alpha-2:compile
[DEBUG]    org.codehaus.plexus:plexus-utils:jar:3.0.22:compile
[DEBUG]    org.apache.maven.shared:file-management:jar:1.2.1:compile
[DEBUG]       org.apache.maven.shared:maven-shared-io:jar:1.1:compile
[DEBUG]          org.apache.maven.wagon:wagon-provider-api:jar:1.0-alpha-6:compile
[DEBUG]    org.apache.maven.reporting:maven-reporting-api:jar:2.2.1:compile
[DEBUG]       org.apache.maven.doxia:doxia-sink-api:jar:1.1:compile
[DEBUG]       org.apache.maven.doxia:doxia-logging-api:jar:1.1:compile
[DEBUG]    org.apache.maven.reporting:maven-reporting-impl:jar:2.1:compile
[DEBUG]       org.apache.maven.doxia:doxia-core:jar:1.1.2:compile
[DEBUG]          xerces:xercesImpl:jar:2.8.1:compile
[DEBUG]          commons-lang:commons-lang:jar:2.4:compile
[DEBUG]          commons-httpclient:commons-httpclient:jar:3.1:compile
[DEBUG]             commons-codec:commons-codec:jar:1.2:compile
[DEBUG]       org.apache.maven.doxia:doxia-site-renderer:jar:1.1.2:compile
[DEBUG]          org.apache.maven.doxia:doxia-decoration-model:jar:1.1.2:compile
[DEBUG]          org.apache.maven.doxia:doxia-module-xhtml:jar:1.1.2:compile
[DEBUG]          org.apache.maven.doxia:doxia-module-fml:jar:1.1.2:compile
[DEBUG]          org.codehaus.plexus:plexus-i18n:jar:1.0-beta-7:compile
[DEBUG]          org.codehaus.plexus:plexus-velocity:jar:1.1.7:compile
[DEBUG]          org.apache.velocity:velocity:jar:1.5:compile
[DEBUG]          commons-collections:commons-collections:jar:3.2:compile
[DEBUG]       commons-validator:commons-validator:jar:1.2.0:compile
[DEBUG]          commons-beanutils:commons-beanutils:jar:1.7.0:compile
[DEBUG]          commons-digester:commons-digester:jar:1.6:compile
[DEBUG]          commons-logging:commons-logging:jar:1.0.4:compile
[DEBUG]          oro:oro:jar:2.0.8:compile
[DEBUG]          xml-apis:xml-apis:jar:1.0.b2:compile
[DEBUG]    org.jacoco:org.jacoco.agent:jar:runtime:0.7.7.201606060606:compile
[DEBUG]    org.jacoco:org.jacoco.core:jar:0.7.7.201606060606:compile
[DEBUG]       org.ow2.asm:asm-debug-all:jar:5.1:compile
[DEBUG]    org.jacoco:org.jacoco.report:jar:0.7.7.201606060606:compile
[DEBUG] Created new class realm plugin>org.jacoco:jacoco-maven-plugin:0.7.7.201606060606
[DEBUG] Importing foreign packages into class realm plugin>org.jacoco:jacoco-maven-plugin:0.7.7.201606060606
[DEBUG]   Imported:  < project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Populating class realm plugin>org.jacoco:jacoco-maven-plugin:0.7.7.201606060606
[DEBUG]   Included: org.jacoco:jacoco-maven-plugin:jar:0.7.7.201606060606
[DEBUG]   Included: backport-util-concurrent:backport-util-concurrent:jar:3.1
[DEBUG]   Included: org.codehaus.plexus:plexus-interpolation:jar:1.11
[DEBUG]   Included: junit:junit:jar:4.8.2
[DEBUG]   Included: org.codehaus.plexus:plexus-utils:jar:3.0.22
[DEBUG]   Included: org.apache.maven.shared:file-management:jar:1.2.1
[DEBUG]   Included: org.apache.maven.shared:maven-shared-io:jar:1.1
[DEBUG]   Included: org.apache.maven.reporting:maven-reporting-api:jar:2.2.1
[DEBUG]   Included: org.apache.maven.doxia:doxia-sink-api:jar:1.1
[DEBUG]   Included: org.apache.maven.doxia:doxia-logging-api:jar:1.1
[DEBUG]   Included: org.apache.maven.reporting:maven-reporting-impl:jar:2.1
[DEBUG]   Included: org.apache.maven.doxia:doxia-core:jar:1.1.2
[DEBUG]   Included: xerces:xercesImpl:jar:2.8.1
[DEBUG]   Included: commons-lang:commons-lang:jar:2.4
[DEBUG]   Included: commons-httpclient:commons-httpclient:jar:3.1
[DEBUG]   Included: commons-codec:commons-codec:jar:1.2
[DEBUG]   Included: org.apache.maven.doxia:doxia-site-renderer:jar:1.1.2
[DEBUG]   Included: org.apache.maven.doxia:doxia-decoration-model:jar:1.1.2
[DEBUG]   Included: org.apache.maven.doxia:doxia-module-xhtml:jar:1.1.2
[DEBUG]   Included: org.apache.maven.doxia:doxia-module-fml:jar:1.1.2
[DEBUG]   Included: org.codehaus.plexus:plexus-i18n:jar:1.0-beta-7
[DEBUG]   Included: org.codehaus.plexus:plexus-velocity:jar:1.1.7
[DEBUG]   Included: org.apache.velocity:velocity:jar:1.5
[DEBUG]   Included: commons-collections:commons-collections:jar:3.2
[DEBUG]   Included: commons-validator:commons-validator:jar:1.2.0
[DEBUG]   Included: commons-beanutils:commons-beanutils:jar:1.7.0
[DEBUG]   Included: commons-digester:commons-digester:jar:1.6
[DEBUG]   Included: commons-logging:commons-logging:jar:1.0.4
[DEBUG]   Included: oro:oro:jar:2.0.8
[DEBUG]   Included: xml-apis:xml-apis:jar:1.0.b2
[DEBUG]   Included: org.jacoco:org.jacoco.agent:jar:runtime:0.7.7.201606060606
[DEBUG]   Included: org.jacoco:org.jacoco.core:jar:0.7.7.201606060606
[DEBUG]   Included: org.ow2.asm:asm-debug-all:jar:5.1
[DEBUG]   Included: org.jacoco:org.jacoco.report:jar:0.7.7.201606060606
[DEBUG] Configuring mojo org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:prepare-agent from plugin realm ClassRealm[plugin>org.jacoco:jacoco-maven-plugin:0.7.7.201606060606, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:prepare-agent' with basic configurator -->
[DEBUG]   (f) destFile = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\jacoco.exec
[DEBUG]   (f) pluginArtifactMap = {org.jacoco:jacoco-maven-plugin=org.jacoco:jacoco-maven-plugin:maven-plugin:0.7.7.201606060606:, org.apache.maven:maven-plugin-api=org.apache.maven:maven-plugin-api:jar:2.2.1:compile, org.apache.maven:maven-project=org.apache.maven:maven-project:jar:2.2.1:compile, org.apache.maven:maven-settings=org.apache.maven:maven-settings:jar:2.2.1:compile, org.apache.maven:maven-profile=org.apache.maven:maven-profile:jar:2.2.1:compile, org.apache.maven:maven-model=org.apache.maven:maven-model:jar:2.2.1:compile, org.apache.maven:maven-artifact-manager=org.apache.maven:maven-artifact-manager:jar:2.2.1:compile, org.apache.maven:maven-repository-metadata=org.apache.maven:maven-repository-metadata:jar:2.2.1:compile, backport-util-concurrent:backport-util-concurrent=backport-util-concurrent:backport-util-concurrent:jar:3.1:compile, org.apache.maven:maven-plugin-registry=org.apache.maven:maven-plugin-registry:jar:2.2.1:compile, org.codehaus.plexus:plexus-interpolation=org.codehaus.plexus:plexus-interpolation:jar:1.11:compile, org.apache.maven:maven-artifact=org.apache.maven:maven-artifact:jar:2.2.1:compile, org.codehaus.plexus:plexus-container-default=org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile, junit:junit=junit:junit:jar:4.8.2:compile, classworlds:classworlds=classworlds:classworlds:jar:1.1-alpha-2:compile, org.codehaus.plexus:plexus-utils=org.codehaus.plexus:plexus-utils:jar:3.0.22:compile, org.apache.maven.shared:file-management=org.apache.maven.shared:file-management:jar:1.2.1:compile, org.apache.maven.shared:maven-shared-io=org.apache.maven.shared:maven-shared-io:jar:1.1:compile, org.apache.maven.wagon:wagon-provider-api=org.apache.maven.wagon:wagon-provider-api:jar:1.0-alpha-6:compile, org.apache.maven.reporting:maven-reporting-api=org.apache.maven.reporting:maven-reporting-api:jar:2.2.1:compile, org.apache.maven.doxia:doxia-sink-api=org.apache.maven.doxia:doxia-sink-api:jar:1.1:compile, org.apache.maven.doxia:doxia-logging-api=org.apache.maven.doxia:doxia-logging-api:jar:1.1:compile, org.apache.maven.reporting:maven-reporting-impl=org.apache.maven.reporting:maven-reporting-impl:jar:2.1:compile, org.apache.maven.doxia:doxia-core=org.apache.maven.doxia:doxia-core:jar:1.1.2:compile, xerces:xercesImpl=xerces:xercesImpl:jar:2.8.1:compile, commons-lang:commons-lang=commons-lang:commons-lang:jar:2.4:compile, commons-httpclient:commons-httpclient=commons-httpclient:commons-httpclient:jar:3.1:compile, commons-codec:commons-codec=commons-codec:commons-codec:jar:1.2:compile, org.apache.maven.doxia:doxia-site-renderer=org.apache.maven.doxia:doxia-site-renderer:jar:1.1.2:compile, org.apache.maven.doxia:doxia-decoration-model=org.apache.maven.doxia:doxia-decoration-model:jar:1.1.2:compile, org.apache.maven.doxia:doxia-module-xhtml=org.apache.maven.doxia:doxia-module-xhtml:jar:1.1.2:compile, org.apache.maven.doxia:doxia-module-fml=org.apache.maven.doxia:doxia-module-fml:jar:1.1.2:compile, org.codehaus.plexus:plexus-i18n=org.codehaus.plexus:plexus-i18n:jar:1.0-beta-7:compile, org.codehaus.plexus:plexus-velocity=org.codehaus.plexus:plexus-velocity:jar:1.1.7:compile, org.apache.velocity:velocity=org.apache.velocity:velocity:jar:1.5:compile, commons-collections:commons-collections=commons-collections:commons-collections:jar:3.2:compile, commons-validator:commons-validator=commons-validator:commons-validator:jar:1.2.0:compile, commons-beanutils:commons-beanutils=commons-beanutils:commons-beanutils:jar:1.7.0:compile, commons-digester:commons-digester=commons-digester:commons-digester:jar:1.6:compile, commons-logging:commons-logging=commons-logging:commons-logging:jar:1.0.4:compile, oro:oro=oro:oro:jar:2.0.8:compile, xml-apis:xml-apis=xml-apis:xml-apis:jar:1.0.b2:compile, org.jacoco:org.jacoco.agent=org.jacoco:org.jacoco.agent:jar:runtime:0.7.7.201606060606:compile, org.jacoco:org.jacoco.core=org.jacoco:org.jacoco.core:jar:0.7.7.201606060606:compile, org.ow2.asm:asm-debug-all=org.ow2.asm:asm-debug-all:jar:5.1:compile, org.jacoco:org.jacoco.report=org.jacoco:org.jacoco.report:jar:0.7.7.201606060606:compile}
[DEBUG]   (f) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (f) skip = false
[DEBUG] -- end configuration --
[INFO] argLine set to -javaagent:C:\\Dev\\Maven\\repository\\org\\jacoco\\org.jacoco.agent\\0.7.7.201606060606\\org.jacoco.agent-0.7.7.201606060606-runtime.jar=destfile=C:\\Dev\\RIVM\\rivm-episim\\java\\epidemes-demo-webapp\\target\\jacoco.exec
[INFO] 
[INFO] --- maven-resources-plugin:3.0.1:resources (default-resources) @ epidemes-demo-webapp ---
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=1, ConflictMarker.markTime=0, ConflictMarker.nodeCount=69, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=0, ConflictIdSorter.conflictIdCount=28, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=2, ConflictResolver.conflictItemCount=68, DefaultDependencyCollector.collectTime=71, DefaultDependencyCollector.transformTime=3}
[DEBUG] org.apache.maven.plugins:maven-resources-plugin:jar:3.0.1:
[DEBUG]    org.apache.maven:maven-plugin-api:jar:3.0:compile
[DEBUG]       org.sonatype.sisu:sisu-inject-plexus:jar:1.4.2:compile
[DEBUG]          org.sonatype.sisu:sisu-inject-bean:jar:1.4.2:compile
[DEBUG]             org.sonatype.sisu:sisu-guice:jar:noaop:2.1.7:compile
[DEBUG]    org.apache.maven:maven-core:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-settings-builder:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-repository-metadata:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-model-builder:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-aether-provider:jar:3.0:runtime
[DEBUG]       org.sonatype.aether:aether-impl:jar:1.7:compile
[DEBUG]          org.sonatype.aether:aether-spi:jar:1.7:compile
[DEBUG]       org.sonatype.aether:aether-api:jar:1.7:compile
[DEBUG]       org.sonatype.aether:aether-util:jar:1.7:compile
[DEBUG]       org.codehaus.plexus:plexus-classworlds:jar:2.2.3:compile
[DEBUG]       org.codehaus.plexus:plexus-component-annotations:jar:1.6:compile
[DEBUG]       org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3:compile
[DEBUG]          org.sonatype.plexus:plexus-cipher:jar:1.4:compile
[DEBUG]    org.apache.maven:maven-artifact:jar:3.0:compile
[DEBUG]    org.apache.maven:maven-settings:jar:3.0:compile
[DEBUG]    org.apache.maven:maven-model:jar:3.0:compile
[DEBUG]    org.codehaus.plexus:plexus-utils:jar:3.0.24:compile
[DEBUG]    org.apache.maven.shared:maven-filtering:jar:3.1.1:compile
[DEBUG]       org.apache.maven.shared:maven-shared-utils:jar:3.0.0:compile
[DEBUG]          commons-io:commons-io:jar:2.4:compile
[DEBUG]          com.google.code.findbugs:jsr305:jar:2.0.1:compile
[DEBUG]       org.sonatype.plexus:plexus-build-api:jar:0.0.7:compile
[DEBUG]    org.codehaus.plexus:plexus-interpolation:jar:1.22:compile
[DEBUG] Created new class realm plugin>org.apache.maven.plugins:maven-resources-plugin:3.0.1
[DEBUG] Importing foreign packages into class realm plugin>org.apache.maven.plugins:maven-resources-plugin:3.0.1
[DEBUG]   Imported:  < project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Populating class realm plugin>org.apache.maven.plugins:maven-resources-plugin:3.0.1
[DEBUG]   Included: org.apache.maven.plugins:maven-resources-plugin:jar:3.0.1
[DEBUG]   Included: org.sonatype.sisu:sisu-inject-bean:jar:1.4.2
[DEBUG]   Included: org.sonatype.sisu:sisu-guice:jar:noaop:2.1.7
[DEBUG]   Included: org.sonatype.aether:aether-util:jar:1.7
[DEBUG]   Included: org.codehaus.plexus:plexus-component-annotations:jar:1.6
[DEBUG]   Included: org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3
[DEBUG]   Included: org.sonatype.plexus:plexus-cipher:jar:1.4
[DEBUG]   Included: org.codehaus.plexus:plexus-utils:jar:3.0.24
[DEBUG]   Included: org.apache.maven.shared:maven-filtering:jar:3.1.1
[DEBUG]   Included: org.apache.maven.shared:maven-shared-utils:jar:3.0.0
[DEBUG]   Included: commons-io:commons-io:jar:2.4
[DEBUG]   Included: com.google.code.findbugs:jsr305:jar:2.0.1
[DEBUG]   Included: org.sonatype.plexus:plexus-build-api:jar:0.0.7
[DEBUG]   Included: org.codehaus.plexus:plexus-interpolation:jar:1.22
[DEBUG] Configuring mojo org.apache.maven.plugins:maven-resources-plugin:3.0.1:resources from plugin realm ClassRealm[plugin>org.apache.maven.plugins:maven-resources-plugin:3.0.1, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.apache.maven.plugins:maven-resources-plugin:3.0.1:resources' with basic configurator -->
[DEBUG]   (f) addDefaultExcludes = true
[DEBUG]   (f) buildFilters = []
[DEBUG]   (f) encoding = UTF-8
[DEBUG]   (f) escapeString = \
[DEBUG]   (f) escapeWindowsPaths = true
[DEBUG]   (f) fileNameFiltering = false
[DEBUG]   (s) includeEmptyDirs = false
[DEBUG]   (s) outputDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[DEBUG]   (s) overwrite = false
[DEBUG]   (f) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (s) resources = [Resource {targetPath: null, filtering: false, FileSet {directory: C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\main\resources, PatternSet [includes: {}, excludes: {}]}}]
[DEBUG]   (f) session = org.apache.maven.execution.MavenSession@773bd77b
[DEBUG]   (f) skip = false
[DEBUG]   (f) supportMultiLineFiltering = false
[DEBUG]   (f) useBuildFilters = true
[DEBUG]   (s) useDefaultDelimiters = true
[DEBUG] -- end configuration --
[DEBUG] properties used {file.encoding.pkg=sun.io, env.PROMPT=$P$G, user.language.format=nl, maven-surefire-plugin.version=2.19.1, java.home=C:\Dev\Java\jdk1.8.0_112\jre, lifecycle-mapping.version=1.0.0, jacoco-maven-plugin.version=0.7.7.201606060606, site-maven-plugin.version=0.9, github.global.server=github, classworlds.conf=C:\Dev\Maven\v3.3.9\bin\..\bin\m2.conf, github.repository.name=epidemes, cargo.version=1.6.2, java.endorsed.dirs=C:\Dev\Java\jdk1.8.0_112\jre\lib\endorsed, env.USERNAME=krevelvr, sun.os.patch.level=Service Pack 1, java.vendor.url=http://java.oracle.com/, env.COMPUTERNAME=LT150214, env.=C:=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, maven-antrun-plugin.version=1.8, java.version=1.8.0_112, exec-maven-plugin.version=1.5.0, maven-javadoc-plugin.version=2.10.4, java.vendor.url.bug=http://bugreport.sun.com/bugreport/, env.USERPROFILE=C:\Users\krevelvr, skipTests=false, user.name=krevelvr, encoding=UTF-8, sun.io.unicode.encoding=UnicodeLittle, javax.inject.version=1, sun.jnu.encoding=Cp1252, java.runtime.name=Java(TM) SE Runtime Environment, env.LOCALAPPDATA=C:\Users\krevelvr\AppData\Local, env.WINDOWS_TRACING_LOGFILE=C:\BVTBin\Tests\installpackage\csilogfile.log, env.COMMONPROGRAMW6432=C:\Program Files\Common Files, java.specification.name=Java Platform API Specification, user.timezone=Europe/Berlin, user.script=, path.separator=;, env.MAVEN_CMD_LINE_ARGS=-X clean verify org.codehaus.cargo:cargo-maven2-plugin:run, jacoco.covered-ratio=0.0, env.PROCESSOR_IDENTIFIER=Intel64 Family 6 Model 61 Stepping 4, GenuineIntel, javax.ws.rs-api.version=2.0.1, file.encoding=Cp1252, httpcomponents.version=4.5.1, env.HOME=C:\Users\krevelvr, sun.java.command=org.codehaus.plexus.classworlds.launcher.Launcher -X clean verify org.codehaus.cargo:cargo-maven2-plugin:run, env.NUMBER_OF_PROCESSORS=4, env.APPDATA=C:\Users\krevelvr\AppData\Roaming, coala.version=0.2.0-b6-SNAPSHOT, maven-dependency-plugin.version=2.10, env.WINDIR=C:\WINDOWS, java.io.tmpdir=C:\Users\krevelvr\AppData\Local\Temp\, user.language=en, line.separator=
, project.baseUri=file:/C:/Dev/RIVM/rivm-episim/java/epidemes-demo-webapp/, javaee-api.version=7.0, cargo.container.dist=http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip, env.COMMONPROGRAMFILES=C:\Program Files\Common Files, env.NEWENVIRONMENT1=C:\Program Files (x86)\Vodafone\Vodafone Mobile Broadband\Optimization Client\, java.vm.info=mixed mode, source=1.8, sun.desktop=windows, java.vm.specification.name=Java Virtual Machine Specification, project.reporting.outputEncoding=UTF-8, env.M2_HOME=C:\Dev\Maven\v3.3.9\bin\.., env.PATHEXT=.COM;.EXE;.BAT;.CMD;.VBS;.VBE;.JS;.JSE;.WSF;.WSH;.MSC;.PY;.PYW, env.WDIR=C:\, env.ONEDRIVE=C:\Users\krevelvr\OneDrive, project.build.sourceLevel=1.8, maven-resources-plugin.version=3.0.1, env.LOGONSERVER=\\DCW06-INT-P, env.PSMODULEPATH=C:\WINDOWS\system32\WindowsPowerShell\v1.0\Modules\;c:\Program Files\Microsoft Security Client\MpProvider\, java.awt.printerjob=sun.awt.windows.WPrinterJob, maven-jar-plugin.version=3.0.2, env.PUBLIC=C:\Users\Public, env.USERDOMAIN=ALT, user.country.format=NL, env.VBOX_MSI_INSTALL_PATH=C:\Program Files\Oracle\VirtualBox\, jetty.version=9.3.9.v20160517, env.PROCESSOR_LEVEL=6, env.PROGRAMFILES(X86)=C:\Program Files (x86), maven.build.timestamp=2017-03-30T13:27:24Z, os.name=Windows 7, java.specification.vendor=Oracle Corporation, env.TMP=C:\Users\krevelvr\AppData\Local\Temp, java.vm.name=Java HotSpot(TM) 64-Bit Server VM, env.DOCKER_TOOLBOX_INSTALL_PATH=C:\Dev\Docker Toolbox, env.OS=Windows_NT, java.library.path=C:\Dev\Java\jdk1.8.0_112\bin;C:\WINDOWS\Sun\Java\bin;C:\WINDOWS\system32;C:\WINDOWS;C:\Dev\Python\Python35\Scripts\;C:\Dev\Python\Python35\;C:\Dev\Git\v2.7.2\bin;C:\Dev\Maven\v3.3.9\bin;C:\ProgramData\Oracle\Java\javapath;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\WINDOWS\System32\WindowsPowerShell\v1.0\;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Program Files (x86)\Skype\Phone\;C:\Dev\MiKTeX29\miktex\bin\x64\;C:\Dev\nodejs\;C:\Dev\Vagrant\bin;C:\Dev\Anaconda3;C:\Dev\Anaconda3\Scripts;C:\Dev\Anaconda3\Library\bin;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Users\krevelvr\AppData\Roaming\npm;C:\Dev\Docker Toolbox;., env.PROGRAMW6432=C:\Program Files, env.PATH=C:\Dev\Python\Python35\Scripts\;C:\Dev\Python\Python35\;C:\Dev\Git\v2.7.2\bin;C:\Dev\Maven\v3.3.9\bin;C:\ProgramData\Oracle\Java\javapath;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\WINDOWS\System32\WindowsPowerShell\v1.0\;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Program Files (x86)\Skype\Phone\;C:\Dev\MiKTeX29\miktex\bin\x64\;C:\Dev\nodejs\;C:\Dev\Vagrant\bin;C:\Dev\Anaconda3;C:\Dev\Anaconda3\Scripts;C:\Dev\Anaconda3\Library\bin;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Users\krevelvr\AppData\Roaming\npm;C:\Dev\Docker Toolbox, env.USERDOCS=C:/Dev/, java.class.version=52.0, maven.multiModuleProjectDirectory=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, github.repository.owner=krevelen@gmail.com, env.HOMEDRIVE=C:, cargo.maven.skip=false, env.SYSTEMROOT=C:\WINDOWS, env.COMSPEC=C:\WINDOWS\system32\cmd.exe, maven.test.skip=false, sun.boot.library.path=C:\Dev\Java\jdk1.8.0_112\jre\bin, project.build.sourceEncoding=UTF-8, env.SYSTEMDRIVE=C:, github.repository.localid=internal.repo, env.PROCESSOR_REVISION=3d04, sun.management.compiler=HotSpot 64-Bit Tiered Compilers, java.awt.graphicsenv=sun.awt.Win32GraphicsEnvironment, user.variant=, maven-shade-plugin.version=2.4.3, hamcrest.version=1.3, javax.servlet-api.version=3.1.0, junit.version=4.11, env.PROGRAMFILES=C:\Program Files, wildfly-maven-plugin.version=1.1.0.Final, java.vm.specification.version=1.8, build-helper-maven-plugin.version=1.10, env.MAVEN_JAVA_EXE="C:\Dev\Java\jdk1.8.0_112\bin\java.exe", env.PROGRAMDATA=C:\ProgramData, awt.toolkit=sun.awt.windows.WToolkit, sun.cpu.isalist=amd64, sun.stderr.encoding=cp850, env.MAVEN_PROJECTBASEDIR=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, java.ext.dirs=C:\Dev\Java\jdk1.8.0_112\jre\lib\ext;C:\WINDOWS\Sun\Java\lib\ext, maven-compiler-plugin.version=3.5.1, os.version=6.1, env.ERROR_CODE=0, user.home=C:\Users\krevelvr, java.vm.vendor=Oracle Corporation, cargo.container=wildfly10x, maven-clean-plugin.version=3.0.0, env.USERDNSDOMAIN=ALT.RIVM.NL, env.JAVA_HOME=C:\Dev\Java\jdk1.8.0_112, user.dir=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, env.COMMONPROGRAMFILES(X86)=C:\Program Files (x86)\Common Files, github.repository.tmpdir=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.., env.FP_NO_HOST_CHECK=NO, concurrentunit.version=0.4.2, log4j.version=2.6.2, sun.cpu.endian=little, env.ALLUSERSPROFILE=C:\ProgramData, env.CLASSWORLDS_LAUNCHER=org.codehaus.plexus.classworlds.launcher.Launcher, env.PROCESSOR_ARCHITECTURE=AMD64, java.vm.version=25.112-b15, wagon-ssh.version=1.0-beta-7, env.HOMEPATH=\Users\krevelvr, org.slf4j.simpleLogger.defaultLogLevel=debug, env.=::=::\, java.class.path=C:\Dev\Maven\v3.3.9\bin\..\boot\plexus-classworlds-2.5.2.jar, env.EXEC_DIR=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, env.UATDATA=C:\WINDOWS\CCM\UATData\D9F8C395-CAB8-491d-B8AC-179A1FE1BE77, os.arch=amd64, maven.build.version=Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T17:41:47+01:00), env.WINDOWS_TRACING_FLAGS=3, env.SESSIONNAME=Console, sun.java.launcher=SUN_STANDARD, java.vm.specification.vendor=Oracle Corporation, github.repository.url=https://raw.github.com/krevelen/epidemes/mvn-repo/, file.separator=\, maven-war-plugin.version=2.6, java.runtime.version=1.8.0_112-b15, sun.boot.class.path=C:\Dev\Java\jdk1.8.0_112\jre\lib\resources.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\rt.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\sunrsasign.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\jsse.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\jce.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\charsets.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\jfr.jar;C:\Dev\Java\jdk1.8.0_112\jre\classes, maven-assembly-plugin.version=2.6, maven.version=3.3.9, jackson.version=2.8.4, env.TEMP=C:\Users\krevelvr\AppData\Local\Temp, maven-source-plugin.version=3.0.0, user.country=US, maven.home=C:\Dev\Maven\v3.3.9\bin\.., maven-deploy-plugin.version=2.8.2, maven-project-info-reports-plugin.version=2.9, java.vendor=Oracle Corporation, github.repository.branch=mvn-repo, env.CLASSWORLDS_JAR="C:\Dev\Maven\v3.3.9\bin\..\boot\plexus-classworlds-2.5.2.jar", java.specification.version=1.8, sun.arch.data.model=64, argLine=-javaagent:C:\\Dev\\Maven\\repository\\org\\jacoco\\org.jacoco.agent\\0.7.7.201606060606\\org.jacoco.agent-0.7.7.201606060606-runtime.jar=destfile=C:\\Dev\\RIVM\\rivm-episim\\java\\epidemes-demo-webapp\\target\\jacoco.exec}
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[DEBUG] resource with targetPath null
directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\main\resources
excludes []
includes []
[DEBUG] ignoreDelta true
[INFO] Copying 0 resource
[DEBUG] no use filter components
[INFO] 
[INFO] --- maven-compiler-plugin:3.5.1:compile (default-compile) @ epidemes-demo-webapp ---
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=1, ConflictMarker.markTime=0, ConflictMarker.nodeCount=188, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=0, ConflictIdSorter.conflictIdCount=51, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=3, ConflictResolver.conflictItemCount=73, DefaultDependencyCollector.collectTime=98, DefaultDependencyCollector.transformTime=4}
[DEBUG] org.apache.maven.plugins:maven-compiler-plugin:jar:3.5.1:
[DEBUG]    org.apache.maven:maven-plugin-api:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-model:jar:3.0:compile
[DEBUG]       org.sonatype.sisu:sisu-inject-plexus:jar:1.4.2:compile
[DEBUG]          org.sonatype.sisu:sisu-inject-bean:jar:1.4.2:compile
[DEBUG]             org.sonatype.sisu:sisu-guice:jar:noaop:2.1.7:compile
[DEBUG]    org.apache.maven:maven-artifact:jar:3.0:compile
[DEBUG]       org.codehaus.plexus:plexus-utils:jar:2.0.4:compile
[DEBUG]    org.apache.maven:maven-core:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-settings:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-settings-builder:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-repository-metadata:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-model-builder:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-aether-provider:jar:3.0:runtime
[DEBUG]       org.sonatype.aether:aether-impl:jar:1.7:compile
[DEBUG]          org.sonatype.aether:aether-spi:jar:1.7:compile
[DEBUG]       org.sonatype.aether:aether-api:jar:1.7:compile
[DEBUG]       org.sonatype.aether:aether-util:jar:1.7:compile
[DEBUG]       org.codehaus.plexus:plexus-interpolation:jar:1.14:compile
[DEBUG]       org.codehaus.plexus:plexus-classworlds:jar:2.2.3:compile
[DEBUG]       org.codehaus.plexus:plexus-component-annotations:jar:1.5.5:compile
[DEBUG]       org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3:compile
[DEBUG]          org.sonatype.plexus:plexus-cipher:jar:1.4:compile
[DEBUG]    org.apache.maven:maven-toolchain:jar:2.2.1:compile
[DEBUG]    org.apache.maven.shared:maven-shared-utils:jar:3.0.0:compile
[DEBUG]       commons-io:commons-io:jar:2.4:compile
[DEBUG]       com.google.code.findbugs:jsr305:jar:2.0.1:compile
[DEBUG]    org.apache.maven.shared:maven-shared-incremental:jar:1.1:compile
[DEBUG]    org.codehaus.plexus:plexus-compiler-api:jar:2.7:compile
[DEBUG]    org.codehaus.plexus:plexus-compiler-manager:jar:2.7:compile
[DEBUG]    org.codehaus.plexus:plexus-compiler-javac:jar:2.7:runtime
[DEBUG] Created new class realm plugin>org.apache.maven.plugins:maven-compiler-plugin:3.5.1
[DEBUG] Importing foreign packages into class realm plugin>org.apache.maven.plugins:maven-compiler-plugin:3.5.1
[DEBUG]   Imported:  < project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Populating class realm plugin>org.apache.maven.plugins:maven-compiler-plugin:3.5.1
[DEBUG]   Included: org.apache.maven.plugins:maven-compiler-plugin:jar:3.5.1
[DEBUG]   Included: org.sonatype.sisu:sisu-inject-bean:jar:1.4.2
[DEBUG]   Included: org.sonatype.sisu:sisu-guice:jar:noaop:2.1.7
[DEBUG]   Included: org.codehaus.plexus:plexus-utils:jar:2.0.4
[DEBUG]   Included: org.sonatype.aether:aether-util:jar:1.7
[DEBUG]   Included: org.codehaus.plexus:plexus-interpolation:jar:1.14
[DEBUG]   Included: org.codehaus.plexus:plexus-component-annotations:jar:1.5.5
[DEBUG]   Included: org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3
[DEBUG]   Included: org.sonatype.plexus:plexus-cipher:jar:1.4
[DEBUG]   Included: org.apache.maven.shared:maven-shared-utils:jar:3.0.0
[DEBUG]   Included: commons-io:commons-io:jar:2.4
[DEBUG]   Included: com.google.code.findbugs:jsr305:jar:2.0.1
[DEBUG]   Included: org.apache.maven.shared:maven-shared-incremental:jar:1.1
[DEBUG]   Included: org.codehaus.plexus:plexus-compiler-api:jar:2.7
[DEBUG]   Included: org.codehaus.plexus:plexus-compiler-manager:jar:2.7
[DEBUG]   Included: org.codehaus.plexus:plexus-compiler-javac:jar:2.7
[DEBUG] Configuring mojo org.apache.maven.plugins:maven-compiler-plugin:3.5.1:compile from plugin realm ClassRealm[plugin>org.apache.maven.plugins:maven-compiler-plugin:3.5.1, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.apache.maven.plugins:maven-compiler-plugin:3.5.1:compile' with basic configurator -->
[DEBUG]   (f) basedir = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp
[DEBUG]   (f) buildDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target
[DEBUG]   (f) classpathElements = [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-maven2-plugin\1.6.2\cargo-maven2-plugin-1.6.2.jar, C:\Dev\Maven\repository\org\apache\maven\maven-archiver\2.4.1\maven-archiver-2.4.1.jar, C:\Dev\Maven\repository\org\apache\maven\maven-artifact\2.0.6\maven-artifact-2.0.6.jar, C:\Dev\Maven\repository\org\apache\maven\maven-model\2.0.6\maven-model-2.0.6.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-archiver\1.0\plexus-archiver-1.0.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-container-default\1.0-alpha-9-stable-1\plexus-container-default-1.0-alpha-9-stable-1.jar, C:\Dev\Maven\repository\junit\junit\4.11\junit-4.11.jar, C:\Dev\Maven\repository\org\hamcrest\hamcrest-core\1.3\hamcrest-core-1.3.jar, C:\Dev\Maven\repository\classworlds\classworlds\1.1-alpha-2\classworlds-1.1-alpha-2.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-io\1.0\plexus-io-1.0.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-utils\2.0.5\plexus-utils-2.0.5.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-interpolation\1.13\plexus-interpolation-1.13.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-generic\1.6.2\cargo-core-api-generic-1.6.2.jar, C:\Dev\Maven\repository\commons-discovery\commons-discovery\0.5\commons-discovery-0.5.jar, C:\Dev\Maven\repository\commons-logging\commons-logging\1.1.1\commons-logging-1.1.1.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-container\1.6.2\cargo-core-api-container-1.6.2.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-module\1.6.2\cargo-core-api-module-1.6.2.jar, C:\Dev\Maven\repository\jaxen\jaxen\1.1.6\jaxen-1.1.6.jar, C:\Dev\Maven\repository\org\jdom\jdom\1.1.3\jdom-1.1.3.jar, C:\Dev\Maven\repository\org\apache\geronimo\specs\geronimo-j2ee-deployment_1.1_spec\1.1\geronimo-j2ee-deployment_1.1_spec-1.1.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-documentation\1.6.2\cargo-documentation-1.6.2.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-daemon-client\1.6.2\cargo-daemon-client-1.6.2.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-util\1.6.2\cargo-core-api-util-1.6.2.jar, C:\Dev\Maven\repository\org\apache\ant\ant\1.7.1\ant-1.7.1.jar, C:\Dev\Maven\repository\org\apache\ant\ant-launcher\1.7.1\ant-launcher-1.7.1.jar]
[DEBUG]   (f) compileSourceRoots = [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\main\java]
[DEBUG]   (f) compilerArgument = -Xlint:unchecked
[DEBUG]   (f) compilerArguments = {Averbose=true, Xlint=null, Xmaxerrs=1000}
[DEBUG]   (f) compilerId = javac
[DEBUG]   (f) debug = true
[DEBUG]   (f) encoding = UTF-8
[DEBUG]   (f) failOnError = true
[DEBUG]   (f) forceJavacCompilerUse = false
[DEBUG]   (f) fork = false
[DEBUG]   (f) generatedSourcesDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\generated-sources\annotations
[DEBUG]   (f) mojoExecution = org.apache.maven.plugins:maven-compiler-plugin:3.5.1:compile {execution: default-compile}
[DEBUG]   (f) optimize = false
[DEBUG]   (f) outputDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[DEBUG]   (f) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (f) projectArtifact = nl.rivm.cib:epidemes-demo-webapp:war:0.1.0-SNAPSHOT
[DEBUG]   (f) session = org.apache.maven.execution.MavenSession@773bd77b
[DEBUG]   (f) showDeprecation = false
[DEBUG]   (f) showWarnings = false
[DEBUG]   (f) skipMultiThreadWarning = false
[DEBUG]   (f) source = 1.8
[DEBUG]   (f) staleMillis = 0
[DEBUG]   (f) target = 1.8
[DEBUG]   (f) useIncrementalCompilation = true
[DEBUG]   (f) verbose = false
[DEBUG] -- end configuration --
[DEBUG] Using compiler 'javac'.
[DEBUG] Source directories: [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\main\java]
[DEBUG] Classpath: [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-maven2-plugin\1.6.2\cargo-maven2-plugin-1.6.2.jar
 C:\Dev\Maven\repository\org\apache\maven\maven-archiver\2.4.1\maven-archiver-2.4.1.jar
 C:\Dev\Maven\repository\org\apache\maven\maven-artifact\2.0.6\maven-artifact-2.0.6.jar
 C:\Dev\Maven\repository\org\apache\maven\maven-model\2.0.6\maven-model-2.0.6.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-archiver\1.0\plexus-archiver-1.0.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-container-default\1.0-alpha-9-stable-1\plexus-container-default-1.0-alpha-9-stable-1.jar
 C:\Dev\Maven\repository\junit\junit\4.11\junit-4.11.jar
 C:\Dev\Maven\repository\org\hamcrest\hamcrest-core\1.3\hamcrest-core-1.3.jar
 C:\Dev\Maven\repository\classworlds\classworlds\1.1-alpha-2\classworlds-1.1-alpha-2.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-io\1.0\plexus-io-1.0.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-utils\2.0.5\plexus-utils-2.0.5.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-interpolation\1.13\plexus-interpolation-1.13.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-generic\1.6.2\cargo-core-api-generic-1.6.2.jar
 C:\Dev\Maven\repository\commons-discovery\commons-discovery\0.5\commons-discovery-0.5.jar
 C:\Dev\Maven\repository\commons-logging\commons-logging\1.1.1\commons-logging-1.1.1.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-container\1.6.2\cargo-core-api-container-1.6.2.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-module\1.6.2\cargo-core-api-module-1.6.2.jar
 C:\Dev\Maven\repository\jaxen\jaxen\1.1.6\jaxen-1.1.6.jar
 C:\Dev\Maven\repository\org\jdom\jdom\1.1.3\jdom-1.1.3.jar
 C:\Dev\Maven\repository\org\apache\geronimo\specs\geronimo-j2ee-deployment_1.1_spec\1.1\geronimo-j2ee-deployment_1.1_spec-1.1.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-documentation\1.6.2\cargo-documentation-1.6.2.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-daemon-client\1.6.2\cargo-daemon-client-1.6.2.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-util\1.6.2\cargo-core-api-util-1.6.2.jar
 C:\Dev\Maven\repository\org\apache\ant\ant\1.7.1\ant-1.7.1.jar
 C:\Dev\Maven\repository\org\apache\ant\ant-launcher\1.7.1\ant-launcher-1.7.1.jar]
[DEBUG] Output directory: C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[DEBUG] Adding C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\generated-sources\annotations to compile source roots:
  C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\main\java
[DEBUG] New compile source roots:
  C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\main\java
  C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\generated-sources\annotations
[DEBUG] CompilerReuseStrategy: reuseCreated
[DEBUG] useIncrementalCompilation enabled
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:3.0.1:testResources (default-testResources) @ epidemes-demo-webapp ---
[DEBUG] Configuring mojo org.apache.maven.plugins:maven-resources-plugin:3.0.1:testResources from plugin realm ClassRealm[plugin>org.apache.maven.plugins:maven-resources-plugin:3.0.1, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.apache.maven.plugins:maven-resources-plugin:3.0.1:testResources' with basic configurator -->
[DEBUG]   (f) addDefaultExcludes = true
[DEBUG]   (f) buildFilters = []
[DEBUG]   (f) encoding = UTF-8
[DEBUG]   (f) escapeString = \
[DEBUG]   (f) escapeWindowsPaths = true
[DEBUG]   (f) fileNameFiltering = false
[DEBUG]   (s) includeEmptyDirs = false
[DEBUG]   (s) outputDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\test-classes
[DEBUG]   (s) overwrite = false
[DEBUG]   (f) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (s) resources = [Resource {targetPath: null, filtering: false, FileSet {directory: C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\test\resources, PatternSet [includes: {}, excludes: {}]}}]
[DEBUG]   (f) session = org.apache.maven.execution.MavenSession@773bd77b
[DEBUG]   (f) skip = false
[DEBUG]   (f) supportMultiLineFiltering = false
[DEBUG]   (f) useBuildFilters = true
[DEBUG]   (s) useDefaultDelimiters = true
[DEBUG] -- end configuration --
[DEBUG] properties used {file.encoding.pkg=sun.io, env.PROMPT=$P$G, user.language.format=nl, maven-surefire-plugin.version=2.19.1, java.home=C:\Dev\Java\jdk1.8.0_112\jre, lifecycle-mapping.version=1.0.0, jacoco-maven-plugin.version=0.7.7.201606060606, site-maven-plugin.version=0.9, github.global.server=github, classworlds.conf=C:\Dev\Maven\v3.3.9\bin\..\bin\m2.conf, github.repository.name=epidemes, cargo.version=1.6.2, java.endorsed.dirs=C:\Dev\Java\jdk1.8.0_112\jre\lib\endorsed, env.USERNAME=krevelvr, sun.os.patch.level=Service Pack 1, java.vendor.url=http://java.oracle.com/, env.COMPUTERNAME=LT150214, env.=C:=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, maven-antrun-plugin.version=1.8, java.version=1.8.0_112, exec-maven-plugin.version=1.5.0, maven-javadoc-plugin.version=2.10.4, java.vendor.url.bug=http://bugreport.sun.com/bugreport/, env.USERPROFILE=C:\Users\krevelvr, skipTests=false, user.name=krevelvr, encoding=UTF-8, sun.io.unicode.encoding=UnicodeLittle, javax.inject.version=1, sun.jnu.encoding=Cp1252, java.runtime.name=Java(TM) SE Runtime Environment, env.LOCALAPPDATA=C:\Users\krevelvr\AppData\Local, env.WINDOWS_TRACING_LOGFILE=C:\BVTBin\Tests\installpackage\csilogfile.log, env.COMMONPROGRAMW6432=C:\Program Files\Common Files, java.specification.name=Java Platform API Specification, user.timezone=Europe/Berlin, user.script=, path.separator=;, env.MAVEN_CMD_LINE_ARGS=-X clean verify org.codehaus.cargo:cargo-maven2-plugin:run, jacoco.covered-ratio=0.0, env.PROCESSOR_IDENTIFIER=Intel64 Family 6 Model 61 Stepping 4, GenuineIntel, javax.ws.rs-api.version=2.0.1, file.encoding=Cp1252, httpcomponents.version=4.5.1, env.HOME=C:\Users\krevelvr, sun.java.command=org.codehaus.plexus.classworlds.launcher.Launcher -X clean verify org.codehaus.cargo:cargo-maven2-plugin:run, env.NUMBER_OF_PROCESSORS=4, env.APPDATA=C:\Users\krevelvr\AppData\Roaming, coala.version=0.2.0-b6-SNAPSHOT, maven-dependency-plugin.version=2.10, env.WINDIR=C:\WINDOWS, java.io.tmpdir=C:\Users\krevelvr\AppData\Local\Temp\, user.language=en, line.separator=
, project.baseUri=file:/C:/Dev/RIVM/rivm-episim/java/epidemes-demo-webapp/, javaee-api.version=7.0, cargo.container.dist=http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip, env.COMMONPROGRAMFILES=C:\Program Files\Common Files, env.NEWENVIRONMENT1=C:\Program Files (x86)\Vodafone\Vodafone Mobile Broadband\Optimization Client\, java.vm.info=mixed mode, source=1.8, sun.desktop=windows, java.vm.specification.name=Java Virtual Machine Specification, project.reporting.outputEncoding=UTF-8, env.M2_HOME=C:\Dev\Maven\v3.3.9\bin\.., env.PATHEXT=.COM;.EXE;.BAT;.CMD;.VBS;.VBE;.JS;.JSE;.WSF;.WSH;.MSC;.PY;.PYW, env.WDIR=C:\, env.ONEDRIVE=C:\Users\krevelvr\OneDrive, project.build.sourceLevel=1.8, maven-resources-plugin.version=3.0.1, env.LOGONSERVER=\\DCW06-INT-P, env.PSMODULEPATH=C:\WINDOWS\system32\WindowsPowerShell\v1.0\Modules\;c:\Program Files\Microsoft Security Client\MpProvider\, java.awt.printerjob=sun.awt.windows.WPrinterJob, maven-jar-plugin.version=3.0.2, env.PUBLIC=C:\Users\Public, env.USERDOMAIN=ALT, user.country.format=NL, env.VBOX_MSI_INSTALL_PATH=C:\Program Files\Oracle\VirtualBox\, jetty.version=9.3.9.v20160517, env.PROCESSOR_LEVEL=6, env.PROGRAMFILES(X86)=C:\Program Files (x86), maven.build.timestamp=2017-03-30T13:27:24Z, os.name=Windows 7, java.specification.vendor=Oracle Corporation, env.TMP=C:\Users\krevelvr\AppData\Local\Temp, java.vm.name=Java HotSpot(TM) 64-Bit Server VM, env.DOCKER_TOOLBOX_INSTALL_PATH=C:\Dev\Docker Toolbox, env.OS=Windows_NT, java.library.path=C:\Dev\Java\jdk1.8.0_112\bin;C:\WINDOWS\Sun\Java\bin;C:\WINDOWS\system32;C:\WINDOWS;C:\Dev\Python\Python35\Scripts\;C:\Dev\Python\Python35\;C:\Dev\Git\v2.7.2\bin;C:\Dev\Maven\v3.3.9\bin;C:\ProgramData\Oracle\Java\javapath;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\WINDOWS\System32\WindowsPowerShell\v1.0\;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Program Files (x86)\Skype\Phone\;C:\Dev\MiKTeX29\miktex\bin\x64\;C:\Dev\nodejs\;C:\Dev\Vagrant\bin;C:\Dev\Anaconda3;C:\Dev\Anaconda3\Scripts;C:\Dev\Anaconda3\Library\bin;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Users\krevelvr\AppData\Roaming\npm;C:\Dev\Docker Toolbox;., env.PROGRAMW6432=C:\Program Files, env.PATH=C:\Dev\Python\Python35\Scripts\;C:\Dev\Python\Python35\;C:\Dev\Git\v2.7.2\bin;C:\Dev\Maven\v3.3.9\bin;C:\ProgramData\Oracle\Java\javapath;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\WINDOWS\System32\WindowsPowerShell\v1.0\;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Program Files (x86)\Skype\Phone\;C:\Dev\MiKTeX29\miktex\bin\x64\;C:\Dev\nodejs\;C:\Dev\Vagrant\bin;C:\Dev\Anaconda3;C:\Dev\Anaconda3\Scripts;C:\Dev\Anaconda3\Library\bin;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Users\krevelvr\AppData\Roaming\npm;C:\Dev\Docker Toolbox, env.USERDOCS=C:/Dev/, java.class.version=52.0, maven.multiModuleProjectDirectory=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, github.repository.owner=krevelen@gmail.com, env.HOMEDRIVE=C:, cargo.maven.skip=false, env.SYSTEMROOT=C:\WINDOWS, env.COMSPEC=C:\WINDOWS\system32\cmd.exe, maven.test.skip=false, sun.boot.library.path=C:\Dev\Java\jdk1.8.0_112\jre\bin, project.build.sourceEncoding=UTF-8, env.SYSTEMDRIVE=C:, github.repository.localid=internal.repo, env.PROCESSOR_REVISION=3d04, sun.management.compiler=HotSpot 64-Bit Tiered Compilers, java.awt.graphicsenv=sun.awt.Win32GraphicsEnvironment, user.variant=, maven-shade-plugin.version=2.4.3, hamcrest.version=1.3, javax.servlet-api.version=3.1.0, junit.version=4.11, env.PROGRAMFILES=C:\Program Files, wildfly-maven-plugin.version=1.1.0.Final, java.vm.specification.version=1.8, build-helper-maven-plugin.version=1.10, env.MAVEN_JAVA_EXE="C:\Dev\Java\jdk1.8.0_112\bin\java.exe", env.PROGRAMDATA=C:\ProgramData, awt.toolkit=sun.awt.windows.WToolkit, sun.cpu.isalist=amd64, sun.stderr.encoding=cp850, env.MAVEN_PROJECTBASEDIR=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, java.ext.dirs=C:\Dev\Java\jdk1.8.0_112\jre\lib\ext;C:\WINDOWS\Sun\Java\lib\ext, maven-compiler-plugin.version=3.5.1, os.version=6.1, env.ERROR_CODE=0, user.home=C:\Users\krevelvr, java.vm.vendor=Oracle Corporation, cargo.container=wildfly10x, maven-clean-plugin.version=3.0.0, env.USERDNSDOMAIN=ALT.RIVM.NL, env.JAVA_HOME=C:\Dev\Java\jdk1.8.0_112, user.dir=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, env.COMMONPROGRAMFILES(X86)=C:\Program Files (x86)\Common Files, github.repository.tmpdir=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.., env.FP_NO_HOST_CHECK=NO, concurrentunit.version=0.4.2, log4j.version=2.6.2, sun.cpu.endian=little, env.ALLUSERSPROFILE=C:\ProgramData, env.CLASSWORLDS_LAUNCHER=org.codehaus.plexus.classworlds.launcher.Launcher, env.PROCESSOR_ARCHITECTURE=AMD64, java.vm.version=25.112-b15, wagon-ssh.version=1.0-beta-7, env.HOMEPATH=\Users\krevelvr, org.slf4j.simpleLogger.defaultLogLevel=debug, env.=::=::\, java.class.path=C:\Dev\Maven\v3.3.9\bin\..\boot\plexus-classworlds-2.5.2.jar, env.EXEC_DIR=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, env.UATDATA=C:\WINDOWS\CCM\UATData\D9F8C395-CAB8-491d-B8AC-179A1FE1BE77, os.arch=amd64, maven.build.version=Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T17:41:47+01:00), env.WINDOWS_TRACING_FLAGS=3, env.SESSIONNAME=Console, sun.java.launcher=SUN_STANDARD, java.vm.specification.vendor=Oracle Corporation, github.repository.url=https://raw.github.com/krevelen/epidemes/mvn-repo/, file.separator=\, maven-war-plugin.version=2.6, java.runtime.version=1.8.0_112-b15, sun.boot.class.path=C:\Dev\Java\jdk1.8.0_112\jre\lib\resources.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\rt.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\sunrsasign.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\jsse.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\jce.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\charsets.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\jfr.jar;C:\Dev\Java\jdk1.8.0_112\jre\classes, maven-assembly-plugin.version=2.6, maven.version=3.3.9, jackson.version=2.8.4, env.TEMP=C:\Users\krevelvr\AppData\Local\Temp, maven-source-plugin.version=3.0.0, user.country=US, maven.home=C:\Dev\Maven\v3.3.9\bin\.., maven-deploy-plugin.version=2.8.2, maven-project-info-reports-plugin.version=2.9, java.vendor=Oracle Corporation, github.repository.branch=mvn-repo, env.CLASSWORLDS_JAR="C:\Dev\Maven\v3.3.9\bin\..\boot\plexus-classworlds-2.5.2.jar", java.specification.version=1.8, sun.arch.data.model=64, argLine=-javaagent:C:\\Dev\\Maven\\repository\\org\\jacoco\\org.jacoco.agent\\0.7.7.201606060606\\org.jacoco.agent-0.7.7.201606060606-runtime.jar=destfile=C:\\Dev\\RIVM\\rivm-episim\\java\\epidemes-demo-webapp\\target\\jacoco.exec}
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[DEBUG] resource with targetPath null
directory C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\test\resources
excludes []
includes []
[DEBUG] ignoreDelta true
[INFO] Copying 0 resource
[DEBUG] no use filter components
[INFO] 
[INFO] --- maven-compiler-plugin:3.5.1:testCompile (default-testCompile) @ epidemes-demo-webapp ---
[DEBUG] Configuring mojo org.apache.maven.plugins:maven-compiler-plugin:3.5.1:testCompile from plugin realm ClassRealm[plugin>org.apache.maven.plugins:maven-compiler-plugin:3.5.1, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.apache.maven.plugins:maven-compiler-plugin:3.5.1:testCompile' with basic configurator -->
[DEBUG]   (f) basedir = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp
[DEBUG]   (f) buildDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target
[DEBUG]   (f) classpathElements = [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\test-classes, C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-maven2-plugin\1.6.2\cargo-maven2-plugin-1.6.2.jar, C:\Dev\Maven\repository\org\apache\maven\maven-archiver\2.4.1\maven-archiver-2.4.1.jar, C:\Dev\Maven\repository\org\apache\maven\maven-artifact\2.0.6\maven-artifact-2.0.6.jar, C:\Dev\Maven\repository\org\apache\maven\maven-model\2.0.6\maven-model-2.0.6.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-archiver\1.0\plexus-archiver-1.0.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-container-default\1.0-alpha-9-stable-1\plexus-container-default-1.0-alpha-9-stable-1.jar, C:\Dev\Maven\repository\junit\junit\4.11\junit-4.11.jar, C:\Dev\Maven\repository\org\hamcrest\hamcrest-core\1.3\hamcrest-core-1.3.jar, C:\Dev\Maven\repository\classworlds\classworlds\1.1-alpha-2\classworlds-1.1-alpha-2.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-io\1.0\plexus-io-1.0.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-utils\2.0.5\plexus-utils-2.0.5.jar, C:\Dev\Maven\repository\org\codehaus\plexus\plexus-interpolation\1.13\plexus-interpolation-1.13.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-generic\1.6.2\cargo-core-api-generic-1.6.2.jar, C:\Dev\Maven\repository\commons-discovery\commons-discovery\0.5\commons-discovery-0.5.jar, C:\Dev\Maven\repository\commons-logging\commons-logging\1.1.1\commons-logging-1.1.1.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-container\1.6.2\cargo-core-api-container-1.6.2.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-module\1.6.2\cargo-core-api-module-1.6.2.jar, C:\Dev\Maven\repository\jaxen\jaxen\1.1.6\jaxen-1.1.6.jar, C:\Dev\Maven\repository\org\jdom\jdom\1.1.3\jdom-1.1.3.jar, C:\Dev\Maven\repository\org\apache\geronimo\specs\geronimo-j2ee-deployment_1.1_spec\1.1\geronimo-j2ee-deployment_1.1_spec-1.1.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-documentation\1.6.2\cargo-documentation-1.6.2.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-daemon-client\1.6.2\cargo-daemon-client-1.6.2.jar, C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-util\1.6.2\cargo-core-api-util-1.6.2.jar, C:\Dev\Maven\repository\org\apache\ant\ant\1.7.1\ant-1.7.1.jar, C:\Dev\Maven\repository\org\apache\ant\ant-launcher\1.7.1\ant-launcher-1.7.1.jar]
[DEBUG]   (f) compileSourceRoots = [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\test\java]
[DEBUG]   (f) compilerArgument = -Xlint:unchecked
[DEBUG]   (f) compilerArguments = {Averbose=true, Xlint=null, Xmaxerrs=1000}
[DEBUG]   (f) compilerId = javac
[DEBUG]   (f) debug = true
[DEBUG]   (f) encoding = UTF-8
[DEBUG]   (f) failOnError = true
[DEBUG]   (f) forceJavacCompilerUse = false
[DEBUG]   (f) fork = false
[DEBUG]   (f) generatedTestSourcesDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\generated-test-sources\test-annotations
[DEBUG]   (f) mojoExecution = org.apache.maven.plugins:maven-compiler-plugin:3.5.1:testCompile {execution: default-testCompile}
[DEBUG]   (f) optimize = false
[DEBUG]   (f) outputDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\test-classes
[DEBUG]   (f) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (f) session = org.apache.maven.execution.MavenSession@773bd77b
[DEBUG]   (f) showDeprecation = false
[DEBUG]   (f) showWarnings = false
[DEBUG]   (f) skip = false
[DEBUG]   (f) skipMultiThreadWarning = false
[DEBUG]   (f) source = 1.8
[DEBUG]   (f) staleMillis = 0
[DEBUG]   (f) target = 1.8
[DEBUG]   (f) useIncrementalCompilation = true
[DEBUG]   (f) verbose = false
[DEBUG] -- end configuration --
[DEBUG] Using compiler 'javac'.
[DEBUG] Source directories: [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\test\java]
[DEBUG] Classpath: [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\test-classes
 C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-maven2-plugin\1.6.2\cargo-maven2-plugin-1.6.2.jar
 C:\Dev\Maven\repository\org\apache\maven\maven-archiver\2.4.1\maven-archiver-2.4.1.jar
 C:\Dev\Maven\repository\org\apache\maven\maven-artifact\2.0.6\maven-artifact-2.0.6.jar
 C:\Dev\Maven\repository\org\apache\maven\maven-model\2.0.6\maven-model-2.0.6.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-archiver\1.0\plexus-archiver-1.0.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-container-default\1.0-alpha-9-stable-1\plexus-container-default-1.0-alpha-9-stable-1.jar
 C:\Dev\Maven\repository\junit\junit\4.11\junit-4.11.jar
 C:\Dev\Maven\repository\org\hamcrest\hamcrest-core\1.3\hamcrest-core-1.3.jar
 C:\Dev\Maven\repository\classworlds\classworlds\1.1-alpha-2\classworlds-1.1-alpha-2.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-io\1.0\plexus-io-1.0.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-utils\2.0.5\plexus-utils-2.0.5.jar
 C:\Dev\Maven\repository\org\codehaus\plexus\plexus-interpolation\1.13\plexus-interpolation-1.13.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-generic\1.6.2\cargo-core-api-generic-1.6.2.jar
 C:\Dev\Maven\repository\commons-discovery\commons-discovery\0.5\commons-discovery-0.5.jar
 C:\Dev\Maven\repository\commons-logging\commons-logging\1.1.1\commons-logging-1.1.1.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-container\1.6.2\cargo-core-api-container-1.6.2.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-module\1.6.2\cargo-core-api-module-1.6.2.jar
 C:\Dev\Maven\repository\jaxen\jaxen\1.1.6\jaxen-1.1.6.jar
 C:\Dev\Maven\repository\org\jdom\jdom\1.1.3\jdom-1.1.3.jar
 C:\Dev\Maven\repository\org\apache\geronimo\specs\geronimo-j2ee-deployment_1.1_spec\1.1\geronimo-j2ee-deployment_1.1_spec-1.1.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-documentation\1.6.2\cargo-documentation-1.6.2.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-daemon-client\1.6.2\cargo-daemon-client-1.6.2.jar
 C:\Dev\Maven\repository\org\codehaus\cargo\cargo-core-api-util\1.6.2\cargo-core-api-util-1.6.2.jar
 C:\Dev\Maven\repository\org\apache\ant\ant\1.7.1\ant-1.7.1.jar
 C:\Dev\Maven\repository\org\apache\ant\ant-launcher\1.7.1\ant-launcher-1.7.1.jar]
[DEBUG] Output directory: C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\test-classes
[DEBUG] Adding C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\generated-test-sources\test-annotations to test-compile source roots:
  C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\test\java
[DEBUG] New test-compile source roots:
  C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\test\java
  C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\generated-test-sources\test-annotations
[DEBUG] CompilerReuseStrategy: reuseCreated
[DEBUG] useIncrementalCompilation enabled
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:2.19.1:test (default-test) @ epidemes-demo-webapp ---
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=0, ConflictMarker.markTime=0, ConflictMarker.nodeCount=114, ConflictIdSorter.graphTime=1, ConflictIdSorter.topsortTime=0, ConflictIdSorter.conflictIdCount=34, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=1, ConflictResolver.conflictItemCount=80, DefaultDependencyCollector.collectTime=37, DefaultDependencyCollector.transformTime=2}
[DEBUG] org.apache.maven.plugins:maven-surefire-plugin:jar:2.19.1:
[DEBUG]    org.apache.maven:maven-plugin-api:jar:2.2.1:compile
[DEBUG]    org.apache.maven.surefire:maven-surefire-common:jar:2.19.1:compile
[DEBUG]       org.apache.maven.surefire:surefire-booter:jar:2.19.1:compile
[DEBUG]       org.apache.maven:maven-artifact:jar:2.2.1:compile
[DEBUG]          org.codehaus.plexus:plexus-utils:jar:1.5.15:compile
[DEBUG]       org.apache.maven:maven-plugin-descriptor:jar:2.2.1:compile
[DEBUG]          org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile
[DEBUG]             junit:junit:jar:4.12:compile
[DEBUG]                org.hamcrest:hamcrest-core:jar:1.3:compile
[DEBUG]       org.apache.maven:maven-project:jar:2.2.1:compile
[DEBUG]          org.apache.maven:maven-settings:jar:2.2.1:compile
[DEBUG]          org.apache.maven:maven-profile:jar:2.2.1:compile
[DEBUG]          org.apache.maven:maven-artifact-manager:jar:2.2.1:compile
[DEBUG]             backport-util-concurrent:backport-util-concurrent:jar:3.1:compile
[DEBUG]          org.apache.maven:maven-plugin-registry:jar:2.2.1:compile
[DEBUG]          org.codehaus.plexus:plexus-interpolation:jar:1.11:compile
[DEBUG]       org.apache.maven:maven-model:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-core:jar:2.2.1:compile
[DEBUG]          org.apache.maven:maven-plugin-parameter-documenter:jar:2.2.1:compile
[DEBUG]          org.slf4j:slf4j-jdk14:jar:1.5.6:runtime
[DEBUG]             org.slf4j:slf4j-api:jar:1.5.6:runtime
[DEBUG]          org.slf4j:jcl-over-slf4j:jar:1.5.6:runtime
[DEBUG]          org.apache.maven.reporting:maven-reporting-api:jar:3.0:compile
[DEBUG]          org.apache.maven:maven-repository-metadata:jar:2.2.1:compile
[DEBUG]          org.apache.maven:maven-error-diagnostics:jar:2.2.1:compile
[DEBUG]          org.apache.maven:maven-monitor:jar:2.2.1:compile
[DEBUG]          classworlds:classworlds:jar:1.1:compile
[DEBUG]          org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3:compile
[DEBUG]             org.sonatype.plexus:plexus-cipher:jar:1.4:compile
[DEBUG]       org.apache.commons:commons-lang3:jar:3.1:compile
[DEBUG]    org.apache.maven.surefire:surefire-api:jar:2.19.1:compile
[DEBUG]    org.apache.maven:maven-toolchain:jar:2.2.1:compile
[DEBUG]    org.apache.maven.plugin-tools:maven-plugin-annotations:jar:3.3:compile
[DEBUG] Created new class realm plugin>org.apache.maven.plugins:maven-surefire-plugin:2.19.1
[DEBUG] Importing foreign packages into class realm plugin>org.apache.maven.plugins:maven-surefire-plugin:2.19.1
[DEBUG]   Imported:  < project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Populating class realm plugin>org.apache.maven.plugins:maven-surefire-plugin:2.19.1
[DEBUG]   Included: org.apache.maven.plugins:maven-surefire-plugin:jar:2.19.1
[DEBUG]   Included: org.apache.maven.surefire:maven-surefire-common:jar:2.19.1
[DEBUG]   Included: org.apache.maven.surefire:surefire-booter:jar:2.19.1
[DEBUG]   Included: org.codehaus.plexus:plexus-utils:jar:1.5.15
[DEBUG]   Included: junit:junit:jar:4.12
[DEBUG]   Included: org.hamcrest:hamcrest-core:jar:1.3
[DEBUG]   Included: backport-util-concurrent:backport-util-concurrent:jar:3.1
[DEBUG]   Included: org.codehaus.plexus:plexus-interpolation:jar:1.11
[DEBUG]   Included: org.slf4j:slf4j-jdk14:jar:1.5.6
[DEBUG]   Included: org.slf4j:jcl-over-slf4j:jar:1.5.6
[DEBUG]   Included: org.apache.maven.reporting:maven-reporting-api:jar:3.0
[DEBUG]   Included: org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3
[DEBUG]   Included: org.sonatype.plexus:plexus-cipher:jar:1.4
[DEBUG]   Included: org.apache.commons:commons-lang3:jar:3.1
[DEBUG]   Included: org.apache.maven.surefire:surefire-api:jar:2.19.1
[DEBUG]   Included: org.apache.maven.plugin-tools:maven-plugin-annotations:jar:3.3
[DEBUG] Configuring mojo org.apache.maven.plugins:maven-surefire-plugin:2.19.1:test from plugin realm ClassRealm[plugin>org.apache.maven.plugins:maven-surefire-plugin:2.19.1, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.apache.maven.plugins:maven-surefire-plugin:2.19.1:test' with basic configurator -->
[DEBUG]   (s) additionalClasspathElements = []
[DEBUG]   (s) argLine = -javaagent:C:\\Dev\\Maven\\repository\\org\\jacoco\\org.jacoco.agent\\0.7.7.201606060606\\org.jacoco.agent-0.7.7.201606060606-runtime.jar=destfile=C:\\Dev\\RIVM\\rivm-episim\\java\\epidemes-demo-webapp\\target\\jacoco.exec
[DEBUG]   (s) basedir = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp
[DEBUG]   (s) childDelegation = false
[DEBUG]   (s) classesDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[DEBUG]   (s) classpathDependencyExcludes = []
[DEBUG]   (s) dependenciesToScan = []
[DEBUG]   (s) disableXmlReport = false
[DEBUG]   (s) enableAssertions = true
[DEBUG]   (f) forkCount = 1
[DEBUG]   (s) forkMode = once
[DEBUG]   (s) junitArtifactName = junit:junit
[DEBUG]   (s) localRepository =       id: local
      url: file:///C:/Dev/Maven/repository/
   layout: default
snapshots: [enabled => true, update => always]
 releases: [enabled => true, update => always]

[DEBUG]   (s) parallel = methods
[DEBUG]   (f) parallelMavenExecution = false
[DEBUG]   (s) parallelOptimized = true
[DEBUG]   (s) perCoreThreadCount = true
[DEBUG]   (s) pluginArtifactMap = {org.apache.maven.plugins:maven-surefire-plugin=org.apache.maven.plugins:maven-surefire-plugin:maven-plugin:2.19.1:, org.apache.maven:maven-plugin-api=org.apache.maven:maven-plugin-api:jar:2.2.1:compile, org.apache.maven.surefire:maven-surefire-common=org.apache.maven.surefire:maven-surefire-common:jar:2.19.1:compile, org.apache.maven.surefire:surefire-booter=org.apache.maven.surefire:surefire-booter:jar:2.19.1:compile, org.apache.maven:maven-artifact=org.apache.maven:maven-artifact:jar:2.2.1:compile, org.codehaus.plexus:plexus-utils=org.codehaus.plexus:plexus-utils:jar:1.5.15:compile, org.apache.maven:maven-plugin-descriptor=org.apache.maven:maven-plugin-descriptor:jar:2.2.1:compile, org.codehaus.plexus:plexus-container-default=org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile, junit:junit=junit:junit:jar:4.12:compile, org.hamcrest:hamcrest-core=org.hamcrest:hamcrest-core:jar:1.3:compile, org.apache.maven:maven-project=org.apache.maven:maven-project:jar:2.2.1:compile, org.apache.maven:maven-settings=org.apache.maven:maven-settings:jar:2.2.1:compile, org.apache.maven:maven-profile=org.apache.maven:maven-profile:jar:2.2.1:compile, org.apache.maven:maven-artifact-manager=org.apache.maven:maven-artifact-manager:jar:2.2.1:compile, backport-util-concurrent:backport-util-concurrent=backport-util-concurrent:backport-util-concurrent:jar:3.1:compile, org.apache.maven:maven-plugin-registry=org.apache.maven:maven-plugin-registry:jar:2.2.1:compile, org.codehaus.plexus:plexus-interpolation=org.codehaus.plexus:plexus-interpolation:jar:1.11:compile, org.apache.maven:maven-model=org.apache.maven:maven-model:jar:2.2.1:compile, org.apache.maven:maven-core=org.apache.maven:maven-core:jar:2.2.1:compile, org.apache.maven:maven-plugin-parameter-documenter=org.apache.maven:maven-plugin-parameter-documenter:jar:2.2.1:compile, org.slf4j:slf4j-jdk14=org.slf4j:slf4j-jdk14:jar:1.5.6:runtime, org.slf4j:slf4j-api=org.slf4j:slf4j-api:jar:1.5.6:runtime, org.slf4j:jcl-over-slf4j=org.slf4j:jcl-over-slf4j:jar:1.5.6:runtime, org.apache.maven.reporting:maven-reporting-api=org.apache.maven.reporting:maven-reporting-api:jar:3.0:compile, org.apache.maven:maven-repository-metadata=org.apache.maven:maven-repository-metadata:jar:2.2.1:compile, org.apache.maven:maven-error-diagnostics=org.apache.maven:maven-error-diagnostics:jar:2.2.1:compile, org.apache.maven:maven-monitor=org.apache.maven:maven-monitor:jar:2.2.1:compile, classworlds:classworlds=classworlds:classworlds:jar:1.1:compile, org.sonatype.plexus:plexus-sec-dispatcher=org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3:compile, org.sonatype.plexus:plexus-cipher=org.sonatype.plexus:plexus-cipher:jar:1.4:compile, org.apache.commons:commons-lang3=org.apache.commons:commons-lang3:jar:3.1:compile, org.apache.maven.surefire:surefire-api=org.apache.maven.surefire:surefire-api:jar:2.19.1:compile, org.apache.maven:maven-toolchain=org.apache.maven:maven-toolchain:jar:2.2.1:compile, org.apache.maven.plugin-tools:maven-plugin-annotations=org.apache.maven.plugin-tools:maven-plugin-annotations:jar:3.3:compile}
[DEBUG]   (f) pluginDescriptor = Component Descriptor: role: 'org.apache.maven.plugin.Mojo', implementation: 'org.apache.maven.plugin.surefire.HelpMojo', role hint: 'org.apache.maven.plugins:maven-surefire-plugin:2.19.1:help'
role: 'org.apache.maven.plugin.Mojo', implementation: 'org.apache.maven.plugin.surefire.SurefirePlugin', role hint: 'org.apache.maven.plugins:maven-surefire-plugin:2.19.1:test'
---
[DEBUG]   (s) printSummary = true
[DEBUG]   (s) projectArtifactMap = {org.codehaus.cargo:cargo-maven2-plugin=org.codehaus.cargo:cargo-maven2-plugin:jar:1.6.2:compile, org.apache.maven:maven-archiver=org.apache.maven:maven-archiver:jar:2.4.1:compile, org.apache.maven:maven-artifact=org.apache.maven:maven-artifact:jar:2.0.6:compile, org.apache.maven:maven-model=org.apache.maven:maven-model:jar:2.0.6:compile, org.codehaus.plexus:plexus-archiver=org.codehaus.plexus:plexus-archiver:jar:1.0:compile, org.codehaus.plexus:plexus-container-default=org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile, junit:junit=junit:junit:jar:4.11:compile, org.hamcrest:hamcrest-core=org.hamcrest:hamcrest-core:jar:1.3:compile, classworlds:classworlds=classworlds:classworlds:jar:1.1-alpha-2:compile, org.codehaus.plexus:plexus-io=org.codehaus.plexus:plexus-io:jar:1.0:compile, org.codehaus.plexus:plexus-utils=org.codehaus.plexus:plexus-utils:jar:2.0.5:compile, org.codehaus.plexus:plexus-interpolation=org.codehaus.plexus:plexus-interpolation:jar:1.13:compile, org.codehaus.cargo:cargo-core-api-generic=org.codehaus.cargo:cargo-core-api-generic:jar:1.6.2:compile, commons-discovery:commons-discovery=commons-discovery:commons-discovery:jar:0.5:compile, commons-logging:commons-logging=commons-logging:commons-logging:jar:1.1.1:compile, org.codehaus.cargo:cargo-core-api-container=org.codehaus.cargo:cargo-core-api-container:jar:1.6.2:compile, org.codehaus.cargo:cargo-core-api-module=org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:compile, jaxen:jaxen=jaxen:jaxen:jar:1.1.6:compile, org.jdom:jdom=org.jdom:jdom:jar:1.1.3:compile, org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec=org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:compile, org.codehaus.cargo:cargo-documentation=org.codehaus.cargo:cargo-documentation:jar:1.6.2:compile, org.codehaus.cargo:cargo-daemon-client=org.codehaus.cargo:cargo-daemon-client:jar:1.6.2:compile, org.codehaus.cargo:cargo-core-api-util=org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:compile, org.apache.ant:ant=org.apache.ant:ant:jar:1.7.1:compile, org.apache.ant:ant-launcher=org.apache.ant:ant-launcher:jar:1.7.1:compile}
[DEBUG]   (s) redirectTestOutputToFile = false
[DEBUG]   (s) remoteRepositories = [      id: central
      url: https://repo.maven.apache.org/maven2
   layout: default
snapshots: [enabled => false, update => daily]
 releases: [enabled => true, update => never]
]
[DEBUG]   (s) reportFormat = brief
[DEBUG]   (s) reportsDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\surefire-reports
[DEBUG]   (f) rerunFailingTestsCount = 0
[DEBUG]   (f) reuseForks = true
[DEBUG]   (s) runOrder = filesystem
[DEBUG]   (f) shutdown = testset
[DEBUG]   (s) skip = false
[DEBUG]   (f) skipAfterFailureCount = 0
[DEBUG]   (s) skipTests = false
[DEBUG]   (s) suiteXmlFiles = []
[DEBUG]   (s) testClassesDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\test-classes
[DEBUG]   (s) testFailureIgnore = false
[DEBUG]   (s) testNGArtifactName = org.testng:testng
[DEBUG]   (s) testSourceDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\test\java
[DEBUG]   (s) threadCount = 1
[DEBUG]   (s) threadCountClasses = 0
[DEBUG]   (s) threadCountMethods = 0
[DEBUG]   (s) threadCountSuites = 0
[DEBUG]   (s) trimStackTrace = true
[DEBUG]   (s) useFile = true
[DEBUG]   (s) useManifestOnlyJar = true
[DEBUG]   (s) useSystemClassLoader = true
[DEBUG]   (s) useUnlimitedThreads = false
[DEBUG]   (s) workingDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp
[DEBUG]   (s) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (s) session = org.apache.maven.execution.MavenSession@773bd77b
[DEBUG] -- end configuration --
[INFO] 
[INFO] --- jacoco-maven-plugin:0.7.7.201606060606:report (default-report) @ epidemes-demo-webapp ---
[DEBUG] Configuring mojo org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:report from plugin realm ClassRealm[plugin>org.jacoco:jacoco-maven-plugin:0.7.7.201606060606, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Setting property: classpath.resource.loader.class => 'org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader'.
[DEBUG] Setting property: site.resource.loader.class => 'org.codehaus.plexus.velocity.SiteResourceLoader'.
[DEBUG] Setting property: velocimacro.messages.on => 'false'.
[DEBUG] Setting property: resource.loader => 'classpath,site'.
[DEBUG] Setting property: runtime.log.invalid.references => 'false'.
[DEBUG] Setting property: resource.manager.logwhenfound => 'false'.
[DEBUG] Setting property: velocimacro.permissions.allow.inline.to.replace.global => 'true'.
[DEBUG] *******************************************************************
[DEBUG] Starting Apache Velocity v1.5 (compiled: 2007-02-22 08:52:29)
[DEBUG] RuntimeInstance initializing.
[DEBUG] Default Properties File: org\apache\velocity\runtime\defaults\velocity.properties
[DEBUG] LogSystem has been deprecated. Please use a LogChute implementation.
[DEBUG] Default ResourceManager initializing. (class org.apache.velocity.runtime.resource.ResourceManagerImpl)
[DEBUG] ResourceLoader instantiated: org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader
[DEBUG] ResourceLoader instantiated: org.codehaus.plexus.velocity.SiteResourceLoader
[DEBUG] ResourceCache: initialized (class org.apache.velocity.runtime.resource.ResourceCacheImpl)
[DEBUG] Default ResourceManager initialization complete.
[DEBUG] Loaded System Directive: org.apache.velocity.runtime.directive.Literal
[DEBUG] Loaded System Directive: org.apache.velocity.runtime.directive.Macro
[DEBUG] Loaded System Directive: org.apache.velocity.runtime.directive.Parse
[DEBUG] Loaded System Directive: org.apache.velocity.runtime.directive.Include
[DEBUG] Loaded System Directive: org.apache.velocity.runtime.directive.Foreach
[DEBUG] Created '20' parsers.
[DEBUG] Velocimacro : initialization starting.
[DEBUG] Velocimacro : allowInline = true : VMs can be defined inline in templates
[DEBUG] Velocimacro : allowInlineToOverride = true : VMs defined inline may replace previous VM definitions
[DEBUG] Velocimacro : allowInlineLocal = false : VMs defined inline will be global in scope if allowed.
[DEBUG] Velocimacro : autoload off : VM system will not automatically reload global library macros
[DEBUG] Velocimacro : Velocimacro : initialization complete.
[DEBUG] RuntimeInstance successfully initialized.
[DEBUG] Configuring mojo 'org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:report' with basic configurator -->
[DEBUG]   (f) dataFile = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\jacoco.exec
[DEBUG]   (f) outputDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\site\jacoco
[DEBUG]   (f) outputEncoding = UTF-8
[DEBUG]   (f) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (f) skip = false
[DEBUG]   (f) sourceEncoding = UTF-8
[DEBUG]   (f) title = epidemes-demo-webapp
[DEBUG] -- end configuration --
[INFO] Skipping JaCoCo execution due to missing execution data file.
[INFO] 
[INFO] --- maven-war-plugin:2.6:war (default-war) @ epidemes-demo-webapp ---
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=0, ConflictMarker.markTime=0, ConflictMarker.nodeCount=120, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=0, ConflictIdSorter.conflictIdCount=45, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=3, ConflictResolver.conflictItemCount=108, DefaultDependencyCollector.collectTime=83, DefaultDependencyCollector.transformTime=3}
[DEBUG] org.apache.maven.plugins:maven-war-plugin:jar:2.6:
[DEBUG]    org.apache.maven:maven-plugin-api:jar:2.2.1:compile
[DEBUG]    org.apache.maven:maven-artifact:jar:2.2.1:compile
[DEBUG]    org.apache.maven:maven-model:jar:2.2.1:compile
[DEBUG]    org.apache.maven:maven-project:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-profile:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-artifact-manager:jar:2.2.1:compile
[DEBUG]          backport-util-concurrent:backport-util-concurrent:jar:3.1:compile
[DEBUG]       org.apache.maven:maven-plugin-registry:jar:2.2.1:compile
[DEBUG]       org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile
[DEBUG]          junit:junit:jar:3.8.1:compile
[DEBUG]    org.apache.maven:maven-core:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-plugin-parameter-documenter:jar:2.2.1:compile
[DEBUG]       org.slf4j:slf4j-jdk14:jar:1.5.6:runtime
[DEBUG]          org.slf4j:slf4j-api:jar:1.5.6:runtime
[DEBUG]       org.slf4j:jcl-over-slf4j:jar:1.5.6:runtime
[DEBUG]       org.apache.maven.reporting:maven-reporting-api:jar:2.2.1:compile
[DEBUG]          org.apache.maven.doxia:doxia-sink-api:jar:1.1:compile
[DEBUG]          org.apache.maven.doxia:doxia-logging-api:jar:1.1:compile
[DEBUG]       org.apache.maven:maven-repository-metadata:jar:2.2.1:compile
[DEBUG]       org.apache.maven:maven-error-diagnostics:jar:2.2.1:compile
[DEBUG]       commons-cli:commons-cli:jar:1.2:compile
[DEBUG]       org.apache.maven:maven-plugin-descriptor:jar:2.2.1:compile
[DEBUG]       org.codehaus.plexus:plexus-interactivity-api:jar:1.0-alpha-4:compile
[DEBUG]       org.apache.maven:maven-monitor:jar:2.2.1:compile
[DEBUG]       classworlds:classworlds:jar:1.1:compile
[DEBUG]       org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3:compile
[DEBUG]          org.sonatype.plexus:plexus-cipher:jar:1.4:compile
[DEBUG]    org.apache.maven:maven-settings:jar:2.2.1:compile
[DEBUG]    org.apache.maven:maven-archiver:jar:2.6:compile
[DEBUG]       org.apache.maven.shared:maven-shared-utils:jar:0.7:compile
[DEBUG]          com.google.code.findbugs:jsr305:jar:2.0.1:compile
[DEBUG]    org.codehaus.plexus:plexus-io:jar:2.4.1:compile
[DEBUG]    commons-io:commons-io:jar:2.2:compile
[DEBUG]    org.codehaus.plexus:plexus-archiver:jar:2.9:compile
[DEBUG]       org.apache.commons:commons-compress:jar:1.9:compile
[DEBUG]    org.codehaus.plexus:plexus-interpolation:jar:1.21:compile
[DEBUG]    com.thoughtworks.xstream:xstream:jar:1.4.4:compile
[DEBUG]       xmlpull:xmlpull:jar:1.1.3.1:compile
[DEBUG]       xpp3:xpp3_min:jar:1.1.4c:compile
[DEBUG]    org.codehaus.plexus:plexus-utils:jar:3.0.20:compile
[DEBUG]    org.apache.maven.shared:maven-filtering:jar:1.3:compile
[DEBUG]       org.sonatype.plexus:plexus-build-api:jar:0.0.4:compile
[DEBUG]    org.apache.maven.shared:maven-mapping:jar:1.0:compile
[DEBUG] Created new class realm plugin>org.apache.maven.plugins:maven-war-plugin:2.6
[DEBUG] Importing foreign packages into class realm plugin>org.apache.maven.plugins:maven-war-plugin:2.6
[DEBUG]   Imported:  < project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Populating class realm plugin>org.apache.maven.plugins:maven-war-plugin:2.6
[DEBUG]   Included: org.apache.maven.plugins:maven-war-plugin:jar:2.6
[DEBUG]   Included: backport-util-concurrent:backport-util-concurrent:jar:3.1
[DEBUG]   Included: junit:junit:jar:3.8.1
[DEBUG]   Included: org.slf4j:slf4j-jdk14:jar:1.5.6
[DEBUG]   Included: org.slf4j:jcl-over-slf4j:jar:1.5.6
[DEBUG]   Included: org.apache.maven.reporting:maven-reporting-api:jar:2.2.1
[DEBUG]   Included: org.apache.maven.doxia:doxia-sink-api:jar:1.1
[DEBUG]   Included: org.apache.maven.doxia:doxia-logging-api:jar:1.1
[DEBUG]   Included: commons-cli:commons-cli:jar:1.2
[DEBUG]   Included: org.codehaus.plexus:plexus-interactivity-api:jar:1.0-alpha-4
[DEBUG]   Included: org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3
[DEBUG]   Included: org.sonatype.plexus:plexus-cipher:jar:1.4
[DEBUG]   Included: org.apache.maven:maven-archiver:jar:2.6
[DEBUG]   Included: org.apache.maven.shared:maven-shared-utils:jar:0.7
[DEBUG]   Included: com.google.code.findbugs:jsr305:jar:2.0.1
[DEBUG]   Included: org.codehaus.plexus:plexus-io:jar:2.4.1
[DEBUG]   Included: commons-io:commons-io:jar:2.2
[DEBUG]   Included: org.codehaus.plexus:plexus-archiver:jar:2.9
[DEBUG]   Included: org.apache.commons:commons-compress:jar:1.9
[DEBUG]   Included: org.codehaus.plexus:plexus-interpolation:jar:1.21
[DEBUG]   Included: com.thoughtworks.xstream:xstream:jar:1.4.4
[DEBUG]   Included: xmlpull:xmlpull:jar:1.1.3.1
[DEBUG]   Included: xpp3:xpp3_min:jar:1.1.4c
[DEBUG]   Included: org.codehaus.plexus:plexus-utils:jar:3.0.20
[DEBUG]   Included: org.apache.maven.shared:maven-filtering:jar:1.3
[DEBUG]   Included: org.sonatype.plexus:plexus-build-api:jar:0.0.4
[DEBUG]   Included: org.apache.maven.shared:maven-mapping:jar:1.0
[DEBUG] Configuring mojo org.apache.maven.plugins:maven-war-plugin:2.6:war from plugin realm ClassRealm[plugin>org.apache.maven.plugins:maven-war-plugin:2.6, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.apache.maven.plugins:maven-war-plugin:2.6:war' with basic configurator -->
[DEBUG]   (s) archiveClasses = false
[DEBUG]   (s) attachClasses = false
[DEBUG]   (s) cacheFile = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\war\work\webapp-cache.xml
[DEBUG]   (s) classesClassifier = classes
[DEBUG]   (s) classesDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes
[DEBUG]   (f) escapedBackslashesInFilePath = false
[DEBUG]   (s) failOnMissingWebXml = true
[DEBUG]   (f) filteringDeploymentDescriptors = false
[DEBUG]   (f) includeEmptyDirectories = false
[DEBUG]   (s) outputDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target
[DEBUG]   (s) primaryArtifact = true
[DEBUG]   (s) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (f) recompressZippedFiles = true
[DEBUG]   (f) resourceEncoding = UTF-8
[DEBUG]   (f) session = org.apache.maven.execution.MavenSession@773bd77b
[DEBUG]   (f) supportMultiLineFiltering = false
[DEBUG]   (s) useCache = false
[DEBUG]   (f) useJvmChmod = true
[DEBUG]   (s) warName = epidemes-demo-webapp-0.1.0-SNAPSHOT
[DEBUG]   (s) warSourceDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\main\webapp
[DEBUG]   (s) warSourceIncludes = **
[DEBUG]   (s) webappDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT
[DEBUG]   (s) workDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\war\work
[DEBUG] -- end configuration --
[INFO] Packaging webapp
[INFO] Assembling webapp [epidemes-demo-webapp] in [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT]
[DEBUG] properties used {file.encoding.pkg=sun.io, env.PROMPT=$P$G, user.language.format=nl, maven-surefire-plugin.version=2.19.1, java.home=C:\Dev\Java\jdk1.8.0_112\jre, lifecycle-mapping.version=1.0.0, jacoco-maven-plugin.version=0.7.7.201606060606, site-maven-plugin.version=0.9, github.global.server=github, classworlds.conf=C:\Dev\Maven\v3.3.9\bin\..\bin\m2.conf, github.repository.name=epidemes, cargo.version=1.6.2, java.endorsed.dirs=C:\Dev\Java\jdk1.8.0_112\jre\lib\endorsed, env.USERNAME=krevelvr, sun.os.patch.level=Service Pack 1, java.vendor.url=http://java.oracle.com/, env.COMPUTERNAME=LT150214, env.=C:=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, maven-antrun-plugin.version=1.8, java.version=1.8.0_112, exec-maven-plugin.version=1.5.0, maven-javadoc-plugin.version=2.10.4, java.vendor.url.bug=http://bugreport.sun.com/bugreport/, env.USERPROFILE=C:\Users\krevelvr, skipTests=false, user.name=krevelvr, encoding=UTF-8, sun.io.unicode.encoding=UnicodeLittle, javax.inject.version=1, sun.jnu.encoding=Cp1252, java.runtime.name=Java(TM) SE Runtime Environment, env.LOCALAPPDATA=C:\Users\krevelvr\AppData\Local, env.WINDOWS_TRACING_LOGFILE=C:\BVTBin\Tests\installpackage\csilogfile.log, env.COMMONPROGRAMW6432=C:\Program Files\Common Files, java.specification.name=Java Platform API Specification, user.timezone=Europe/Berlin, user.script=, path.separator=;, env.MAVEN_CMD_LINE_ARGS=-X clean verify org.codehaus.cargo:cargo-maven2-plugin:run, jacoco.covered-ratio=0.0, env.PROCESSOR_IDENTIFIER=Intel64 Family 6 Model 61 Stepping 4, GenuineIntel, javax.ws.rs-api.version=2.0.1, file.encoding=Cp1252, httpcomponents.version=4.5.1, env.HOME=C:\Users\krevelvr, sun.java.command=org.codehaus.plexus.classworlds.launcher.Launcher -X clean verify org.codehaus.cargo:cargo-maven2-plugin:run, env.NUMBER_OF_PROCESSORS=4, env.APPDATA=C:\Users\krevelvr\AppData\Roaming, coala.version=0.2.0-b6-SNAPSHOT, maven-dependency-plugin.version=2.10, env.WINDIR=C:\WINDOWS, java.io.tmpdir=C:\Users\krevelvr\AppData\Local\Temp\, user.language=en, line.separator=
, javaee-api.version=7.0, cargo.container.dist=http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip, env.COMMONPROGRAMFILES=C:\Program Files\Common Files, env.NEWENVIRONMENT1=C:\Program Files (x86)\Vodafone\Vodafone Mobile Broadband\Optimization Client\, java.vm.info=mixed mode, source=1.8, sun.desktop=windows, java.vm.specification.name=Java Virtual Machine Specification, project.reporting.outputEncoding=UTF-8, env.M2_HOME=C:\Dev\Maven\v3.3.9\bin\.., env.PATHEXT=.COM;.EXE;.BAT;.CMD;.VBS;.VBE;.JS;.JSE;.WSF;.WSH;.MSC;.PY;.PYW, env.WDIR=C:\, env.ONEDRIVE=C:\Users\krevelvr\OneDrive, project.build.sourceLevel=1.8, maven-resources-plugin.version=3.0.1, env.LOGONSERVER=\\DCW06-INT-P, env.PSMODULEPATH=C:\WINDOWS\system32\WindowsPowerShell\v1.0\Modules\;c:\Program Files\Microsoft Security Client\MpProvider\, java.awt.printerjob=sun.awt.windows.WPrinterJob, maven-jar-plugin.version=3.0.2, env.PUBLIC=C:\Users\Public, env.USERDOMAIN=ALT, user.country.format=NL, env.VBOX_MSI_INSTALL_PATH=C:\Program Files\Oracle\VirtualBox\, jetty.version=9.3.9.v20160517, env.PROCESSOR_LEVEL=6, env.PROGRAMFILES(X86)=C:\Program Files (x86), os.name=Windows 7, java.specification.vendor=Oracle Corporation, env.TMP=C:\Users\krevelvr\AppData\Local\Temp, java.vm.name=Java HotSpot(TM) 64-Bit Server VM, env.DOCKER_TOOLBOX_INSTALL_PATH=C:\Dev\Docker Toolbox, env.OS=Windows_NT, java.library.path=C:\Dev\Java\jdk1.8.0_112\bin;C:\WINDOWS\Sun\Java\bin;C:\WINDOWS\system32;C:\WINDOWS;C:\Dev\Python\Python35\Scripts\;C:\Dev\Python\Python35\;C:\Dev\Git\v2.7.2\bin;C:\Dev\Maven\v3.3.9\bin;C:\ProgramData\Oracle\Java\javapath;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\WINDOWS\System32\WindowsPowerShell\v1.0\;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Program Files (x86)\Skype\Phone\;C:\Dev\MiKTeX29\miktex\bin\x64\;C:\Dev\nodejs\;C:\Dev\Vagrant\bin;C:\Dev\Anaconda3;C:\Dev\Anaconda3\Scripts;C:\Dev\Anaconda3\Library\bin;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Users\krevelvr\AppData\Roaming\npm;C:\Dev\Docker Toolbox;., env.PROGRAMW6432=C:\Program Files, env.PATH=C:\Dev\Python\Python35\Scripts\;C:\Dev\Python\Python35\;C:\Dev\Git\v2.7.2\bin;C:\Dev\Maven\v3.3.9\bin;C:\ProgramData\Oracle\Java\javapath;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\WINDOWS\System32\WindowsPowerShell\v1.0\;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Program Files (x86)\Skype\Phone\;C:\Dev\MiKTeX29\miktex\bin\x64\;C:\Dev\nodejs\;C:\Dev\Vagrant\bin;C:\Dev\Anaconda3;C:\Dev\Anaconda3\Scripts;C:\Dev\Anaconda3\Library\bin;C:\Program Files\Intel\WiFi\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Users\krevelvr\AppData\Roaming\npm;C:\Dev\Docker Toolbox, env.USERDOCS=C:/Dev/, java.class.version=52.0, maven.multiModuleProjectDirectory=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, github.repository.owner=krevelen@gmail.com, env.HOMEDRIVE=C:, cargo.maven.skip=false, env.SYSTEMROOT=C:\WINDOWS, env.COMSPEC=C:\WINDOWS\system32\cmd.exe, maven.test.skip=false, sun.boot.library.path=C:\Dev\Java\jdk1.8.0_112\jre\bin, project.build.sourceEncoding=UTF-8, env.SYSTEMDRIVE=C:, github.repository.localid=internal.repo, env.PROCESSOR_REVISION=3d04, sun.management.compiler=HotSpot 64-Bit Tiered Compilers, java.awt.graphicsenv=sun.awt.Win32GraphicsEnvironment, user.variant=, maven-shade-plugin.version=2.4.3, hamcrest.version=1.3, javax.servlet-api.version=3.1.0, junit.version=4.11, env.PROGRAMFILES=C:\Program Files, wildfly-maven-plugin.version=1.1.0.Final, java.vm.specification.version=1.8, build-helper-maven-plugin.version=1.10, env.MAVEN_JAVA_EXE="C:\Dev\Java\jdk1.8.0_112\bin\java.exe", env.PROGRAMDATA=C:\ProgramData, awt.toolkit=sun.awt.windows.WToolkit, sun.cpu.isalist=amd64, sun.stderr.encoding=cp850, env.MAVEN_PROJECTBASEDIR=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, java.ext.dirs=C:\Dev\Java\jdk1.8.0_112\jre\lib\ext;C:\WINDOWS\Sun\Java\lib\ext, maven-compiler-plugin.version=3.5.1, os.version=6.1, env.ERROR_CODE=0, user.home=C:\Users\krevelvr, java.vm.vendor=Oracle Corporation, cargo.container=wildfly10x, maven-clean-plugin.version=3.0.0, env.USERDNSDOMAIN=ALT.RIVM.NL, env.JAVA_HOME=C:\Dev\Java\jdk1.8.0_112, user.dir=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, env.COMMONPROGRAMFILES(X86)=C:\Program Files (x86)\Common Files, github.repository.tmpdir=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.., env.FP_NO_HOST_CHECK=NO, concurrentunit.version=0.4.2, log4j.version=2.6.2, sun.cpu.endian=little, env.ALLUSERSPROFILE=C:\ProgramData, env.CLASSWORLDS_LAUNCHER=org.codehaus.plexus.classworlds.launcher.Launcher, env.PROCESSOR_ARCHITECTURE=AMD64, java.vm.version=25.112-b15, wagon-ssh.version=1.0-beta-7, env.HOMEPATH=\Users\krevelvr, org.slf4j.simpleLogger.defaultLogLevel=debug, env.=::=::\, java.class.path=C:\Dev\Maven\v3.3.9\bin\..\boot\plexus-classworlds-2.5.2.jar, env.EXEC_DIR=C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp, env.UATDATA=C:\WINDOWS\CCM\UATData\D9F8C395-CAB8-491d-B8AC-179A1FE1BE77, os.arch=amd64, maven.build.version=Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T17:41:47+01:00), env.WINDOWS_TRACING_FLAGS=3, env.SESSIONNAME=Console, sun.java.launcher=SUN_STANDARD, java.vm.specification.vendor=Oracle Corporation, github.repository.url=https://raw.github.com/krevelen/epidemes/mvn-repo/, file.separator=\, maven-war-plugin.version=2.6, java.runtime.version=1.8.0_112-b15, sun.boot.class.path=C:\Dev\Java\jdk1.8.0_112\jre\lib\resources.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\rt.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\sunrsasign.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\jsse.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\jce.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\charsets.jar;C:\Dev\Java\jdk1.8.0_112\jre\lib\jfr.jar;C:\Dev\Java\jdk1.8.0_112\jre\classes, maven-assembly-plugin.version=2.6, maven.version=3.3.9, jackson.version=2.8.4, env.TEMP=C:\Users\krevelvr\AppData\Local\Temp, maven-source-plugin.version=3.0.0, user.country=US, maven.home=C:\Dev\Maven\v3.3.9\bin\.., maven-deploy-plugin.version=2.8.2, maven-project-info-reports-plugin.version=2.9, java.vendor=Oracle Corporation, github.repository.branch=mvn-repo, env.CLASSWORLDS_JAR="C:\Dev\Maven\v3.3.9\bin\..\boot\plexus-classworlds-2.5.2.jar", java.specification.version=1.8, sun.arch.data.model=64, argLine=-javaagent:C:\\Dev\\Maven\\repository\\org\\jacoco\\org.jacoco.agent\\0.7.7.201606060606\\org.jacoco.agent-0.7.7.201606060606-runtime.jar=destfile=C:\\Dev\\RIVM\\rivm-episim\\java\\epidemes-demo-webapp\\target\\jacoco.exec}
[INFO] Processing war project
[INFO] Copying webapp resources [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\src\main\webapp]
[DEBUG]  + index.html has been copied.
[DEBUG]  + WEB-INF/web.xml has been copied.
[DEBUG] Dump of the current build pathSet content -->
[DEBUG] index.html
[DEBUG] WEB-INF/web.xml
[DEBUG] -- end of dump --
[DEBUG] Processing: cargo-maven2-plugin-1.6.2.jar
[DEBUG]  + WEB-INF/lib/cargo-maven2-plugin-1.6.2.jar has been copied.
[DEBUG] Processing: maven-archiver-2.4.1.jar
[DEBUG]  + WEB-INF/lib/maven-archiver-2.4.1.jar has been copied.
[DEBUG] Processing: maven-artifact-2.0.6.jar
[DEBUG]  + WEB-INF/lib/maven-artifact-2.0.6.jar has been copied.
[DEBUG] Processing: maven-model-2.0.6.jar
[DEBUG]  + WEB-INF/lib/maven-model-2.0.6.jar has been copied.
[DEBUG] Processing: plexus-archiver-1.0.jar
[DEBUG]  + WEB-INF/lib/plexus-archiver-1.0.jar has been copied.
[DEBUG] Processing: plexus-container-default-1.0-alpha-9-stable-1.jar
[DEBUG]  + WEB-INF/lib/plexus-container-default-1.0-alpha-9-stable-1.jar has been copied.
[DEBUG] Processing: junit-4.11.jar
[DEBUG]  + WEB-INF/lib/junit-4.11.jar has been copied.
[DEBUG] Processing: hamcrest-core-1.3.jar
[DEBUG]  + WEB-INF/lib/hamcrest-core-1.3.jar has been copied.
[DEBUG] Processing: classworlds-1.1-alpha-2.jar
[DEBUG]  + WEB-INF/lib/classworlds-1.1-alpha-2.jar has been copied.
[DEBUG] Processing: plexus-io-1.0.jar
[DEBUG]  + WEB-INF/lib/plexus-io-1.0.jar has been copied.
[DEBUG] Processing: plexus-utils-2.0.5.jar
[DEBUG]  + WEB-INF/lib/plexus-utils-2.0.5.jar has been copied.
[DEBUG] Processing: plexus-interpolation-1.13.jar
[DEBUG]  + WEB-INF/lib/plexus-interpolation-1.13.jar has been copied.
[DEBUG] Processing: cargo-core-api-generic-1.6.2.jar
[DEBUG]  + WEB-INF/lib/cargo-core-api-generic-1.6.2.jar has been copied.
[DEBUG] Processing: commons-discovery-0.5.jar
[DEBUG]  + WEB-INF/lib/commons-discovery-0.5.jar has been copied.
[DEBUG] Processing: commons-logging-1.1.1.jar
[DEBUG]  + WEB-INF/lib/commons-logging-1.1.1.jar has been copied.
[DEBUG] Processing: cargo-core-api-container-1.6.2.jar
[DEBUG]  + WEB-INF/lib/cargo-core-api-container-1.6.2.jar has been copied.
[DEBUG] Processing: cargo-core-api-module-1.6.2.jar
[DEBUG]  + WEB-INF/lib/cargo-core-api-module-1.6.2.jar has been copied.
[DEBUG] Processing: jaxen-1.1.6.jar
[DEBUG]  + WEB-INF/lib/jaxen-1.1.6.jar has been copied.
[DEBUG] Processing: jdom-1.1.3.jar
[DEBUG]  + WEB-INF/lib/jdom-1.1.3.jar has been copied.
[DEBUG] Processing: geronimo-j2ee-deployment_1.1_spec-1.1.jar
[DEBUG]  + WEB-INF/lib/geronimo-j2ee-deployment_1.1_spec-1.1.jar has been copied.
[DEBUG] Processing: cargo-documentation-1.6.2.jar
[DEBUG]  + WEB-INF/lib/cargo-documentation-1.6.2.jar has been copied.
[DEBUG] Processing: cargo-daemon-client-1.6.2.jar
[DEBUG]  + WEB-INF/lib/cargo-daemon-client-1.6.2.jar has been copied.
[DEBUG] Processing: cargo-core-api-util-1.6.2.jar
[DEBUG]  + WEB-INF/lib/cargo-core-api-util-1.6.2.jar has been copied.
[DEBUG] Processing: ant-1.7.1.jar
[DEBUG]  + WEB-INF/lib/ant-1.7.1.jar has been copied.
[DEBUG] Processing: ant-launcher-1.7.1.jar
[DEBUG]  + WEB-INF/lib/ant-launcher-1.7.1.jar has been copied.
[INFO] Webapp assembled in [1152 msecs]
[DEBUG] Excluding [] from the generated webapp archive.
[DEBUG] Including [**] in the generated webapp archive.
[INFO] Building war: C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT.war
[DEBUG] adding directory META-INF/
[DEBUG] adding entry META-INF/MANIFEST.MF
[DEBUG] adding directory WEB-INF/
[DEBUG] adding directory WEB-INF/classes/
[DEBUG] adding directory WEB-INF/lib/
[DEBUG] adding entry index.html
[DEBUG] adding entry WEB-INF/lib/ant-1.7.1.jar
[DEBUG] adding entry WEB-INF/lib/ant-launcher-1.7.1.jar
[DEBUG] adding entry WEB-INF/lib/cargo-core-api-container-1.6.2.jar
[DEBUG] adding entry WEB-INF/lib/cargo-core-api-generic-1.6.2.jar
[DEBUG] adding entry WEB-INF/lib/cargo-core-api-module-1.6.2.jar
[DEBUG] adding entry WEB-INF/lib/cargo-core-api-util-1.6.2.jar
[DEBUG] adding entry WEB-INF/lib/cargo-daemon-client-1.6.2.jar
[DEBUG] adding entry WEB-INF/lib/cargo-documentation-1.6.2.jar
[DEBUG] adding entry WEB-INF/lib/cargo-maven2-plugin-1.6.2.jar
[DEBUG] adding entry WEB-INF/lib/classworlds-1.1-alpha-2.jar
[DEBUG] adding entry WEB-INF/lib/commons-discovery-0.5.jar
[DEBUG] adding entry WEB-INF/lib/commons-logging-1.1.1.jar
[DEBUG] adding entry WEB-INF/lib/geronimo-j2ee-deployment_1.1_spec-1.1.jar
[DEBUG] adding entry WEB-INF/lib/hamcrest-core-1.3.jar
[DEBUG] adding entry WEB-INF/lib/jaxen-1.1.6.jar
[DEBUG] adding entry WEB-INF/lib/jdom-1.1.3.jar
[DEBUG] adding entry WEB-INF/lib/junit-4.11.jar
[DEBUG] adding entry WEB-INF/lib/maven-archiver-2.4.1.jar
[DEBUG] adding entry WEB-INF/lib/maven-artifact-2.0.6.jar
[DEBUG] adding entry WEB-INF/lib/maven-model-2.0.6.jar
[DEBUG] adding entry WEB-INF/lib/plexus-archiver-1.0.jar
[DEBUG] adding entry WEB-INF/lib/plexus-container-default-1.0-alpha-9-stable-1.jar
[DEBUG] adding entry WEB-INF/lib/plexus-interpolation-1.13.jar
[DEBUG] adding entry WEB-INF/lib/plexus-io-1.0.jar
[DEBUG] adding entry WEB-INF/lib/plexus-utils-2.0.5.jar
[DEBUG] adding entry WEB-INF/web.xml
[DEBUG] WEB-INF\web.xml already added, skipping
[DEBUG] adding entry META-INF/maven/nl.rivm.cib/epidemes-demo-webapp/pom.xml
[DEBUG] adding entry META-INF/maven/nl.rivm.cib/epidemes-demo-webapp/pom.properties
[INFO] 
[INFO] >>> maven-source-plugin:3.0.0:jar (attach-sources) > generate-sources @ epidemes-demo-webapp >>>
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=0, ConflictMarker.markTime=0, ConflictMarker.nodeCount=33, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=0, ConflictIdSorter.conflictIdCount=25, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=1, ConflictResolver.conflictItemCount=32, DefaultDependencyCollector.collectTime=0, DefaultDependencyCollector.transformTime=1}
[DEBUG] nl.rivm.cib:epidemes-demo-webapp:war:0.1.0-SNAPSHOT
[DEBUG]    org.codehaus.cargo:cargo-maven2-plugin:jar:1.6.2:compile
[DEBUG]       org.apache.maven:maven-archiver:jar:2.4.1:compile
[DEBUG]          org.apache.maven:maven-artifact:jar:2.0.6:compile
[DEBUG]          org.apache.maven:maven-model:jar:2.0.6:compile
[DEBUG]          org.codehaus.plexus:plexus-archiver:jar:1.0:compile
[DEBUG]             org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile
[DEBUG]                junit:junit:jar:4.11:compile (version managed from 3.8.1 by nl.rivm.cib:epidemes:0.1.0-SNAPSHOT)
[DEBUG]                   org.hamcrest:hamcrest-core:jar:1.3:compile
[DEBUG]                classworlds:classworlds:jar:1.1-alpha-2:compile
[DEBUG]             org.codehaus.plexus:plexus-io:jar:1.0:compile
[DEBUG]          org.codehaus.plexus:plexus-utils:jar:2.0.5:compile
[DEBUG]          org.codehaus.plexus:plexus-interpolation:jar:1.13:compile
[DEBUG]       org.codehaus.cargo:cargo-core-api-generic:jar:1.6.2:compile
[DEBUG]          commons-discovery:commons-discovery:jar:0.5:compile
[DEBUG]             commons-logging:commons-logging:jar:1.1.1:compile
[DEBUG]          org.codehaus.cargo:cargo-core-api-container:jar:1.6.2:compile
[DEBUG]             org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:compile
[DEBUG]                jaxen:jaxen:jar:1.1.6:compile
[DEBUG]                org.jdom:jdom:jar:1.1.3:compile
[DEBUG]             org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:compile
[DEBUG]       org.codehaus.cargo:cargo-documentation:jar:1.6.2:compile
[DEBUG]       org.codehaus.cargo:cargo-daemon-client:jar:1.6.2:compile
[DEBUG]          org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:compile
[DEBUG]             org.apache.ant:ant:jar:1.7.1:compile
[DEBUG]                org.apache.ant:ant-launcher:jar:1.7.1:compile
[INFO] 
[INFO] --- jacoco-maven-plugin:0.7.7.201606060606:prepare-agent (default-prepare-agent) @ epidemes-demo-webapp ---
[DEBUG] Configuring mojo org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:prepare-agent from plugin realm ClassRealm[plugin>org.jacoco:jacoco-maven-plugin:0.7.7.201606060606, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.jacoco:jacoco-maven-plugin:0.7.7.201606060606:prepare-agent' with basic configurator -->
[DEBUG]   (f) destFile = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\jacoco.exec
[DEBUG]   (f) pluginArtifactMap = {org.jacoco:jacoco-maven-plugin=org.jacoco:jacoco-maven-plugin:maven-plugin:0.7.7.201606060606:, org.apache.maven:maven-plugin-api=org.apache.maven:maven-plugin-api:jar:2.2.1:compile, org.apache.maven:maven-project=org.apache.maven:maven-project:jar:2.2.1:compile, org.apache.maven:maven-settings=org.apache.maven:maven-settings:jar:2.2.1:compile, org.apache.maven:maven-profile=org.apache.maven:maven-profile:jar:2.2.1:compile, org.apache.maven:maven-model=org.apache.maven:maven-model:jar:2.2.1:compile, org.apache.maven:maven-artifact-manager=org.apache.maven:maven-artifact-manager:jar:2.2.1:compile, org.apache.maven:maven-repository-metadata=org.apache.maven:maven-repository-metadata:jar:2.2.1:compile, backport-util-concurrent:backport-util-concurrent=backport-util-concurrent:backport-util-concurrent:jar:3.1:compile, org.apache.maven:maven-plugin-registry=org.apache.maven:maven-plugin-registry:jar:2.2.1:compile, org.codehaus.plexus:plexus-interpolation=org.codehaus.plexus:plexus-interpolation:jar:1.11:compile, org.apache.maven:maven-artifact=org.apache.maven:maven-artifact:jar:2.2.1:compile, org.codehaus.plexus:plexus-container-default=org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile, junit:junit=junit:junit:jar:4.8.2:compile, classworlds:classworlds=classworlds:classworlds:jar:1.1-alpha-2:compile, org.codehaus.plexus:plexus-utils=org.codehaus.plexus:plexus-utils:jar:3.0.22:compile, org.apache.maven.shared:file-management=org.apache.maven.shared:file-management:jar:1.2.1:compile, org.apache.maven.shared:maven-shared-io=org.apache.maven.shared:maven-shared-io:jar:1.1:compile, org.apache.maven.wagon:wagon-provider-api=org.apache.maven.wagon:wagon-provider-api:jar:1.0-alpha-6:compile, org.apache.maven.reporting:maven-reporting-api=org.apache.maven.reporting:maven-reporting-api:jar:2.2.1:compile, org.apache.maven.doxia:doxia-sink-api=org.apache.maven.doxia:doxia-sink-api:jar:1.1:compile, org.apache.maven.doxia:doxia-logging-api=org.apache.maven.doxia:doxia-logging-api:jar:1.1:compile, org.apache.maven.reporting:maven-reporting-impl=org.apache.maven.reporting:maven-reporting-impl:jar:2.1:compile, org.apache.maven.doxia:doxia-core=org.apache.maven.doxia:doxia-core:jar:1.1.2:compile, xerces:xercesImpl=xerces:xercesImpl:jar:2.8.1:compile, commons-lang:commons-lang=commons-lang:commons-lang:jar:2.4:compile, commons-httpclient:commons-httpclient=commons-httpclient:commons-httpclient:jar:3.1:compile, commons-codec:commons-codec=commons-codec:commons-codec:jar:1.2:compile, org.apache.maven.doxia:doxia-site-renderer=org.apache.maven.doxia:doxia-site-renderer:jar:1.1.2:compile, org.apache.maven.doxia:doxia-decoration-model=org.apache.maven.doxia:doxia-decoration-model:jar:1.1.2:compile, org.apache.maven.doxia:doxia-module-xhtml=org.apache.maven.doxia:doxia-module-xhtml:jar:1.1.2:compile, org.apache.maven.doxia:doxia-module-fml=org.apache.maven.doxia:doxia-module-fml:jar:1.1.2:compile, org.codehaus.plexus:plexus-i18n=org.codehaus.plexus:plexus-i18n:jar:1.0-beta-7:compile, org.codehaus.plexus:plexus-velocity=org.codehaus.plexus:plexus-velocity:jar:1.1.7:compile, org.apache.velocity:velocity=org.apache.velocity:velocity:jar:1.5:compile, commons-collections:commons-collections=commons-collections:commons-collections:jar:3.2:compile, commons-validator:commons-validator=commons-validator:commons-validator:jar:1.2.0:compile, commons-beanutils:commons-beanutils=commons-beanutils:commons-beanutils:jar:1.7.0:compile, commons-digester:commons-digester=commons-digester:commons-digester:jar:1.6:compile, commons-logging:commons-logging=commons-logging:commons-logging:jar:1.0.4:compile, oro:oro=oro:oro:jar:2.0.8:compile, xml-apis:xml-apis=xml-apis:xml-apis:jar:1.0.b2:compile, org.jacoco:org.jacoco.agent=org.jacoco:org.jacoco.agent:jar:runtime:0.7.7.201606060606:compile, org.jacoco:org.jacoco.core=org.jacoco:org.jacoco.core:jar:0.7.7.201606060606:compile, org.ow2.asm:asm-debug-all=org.ow2.asm:asm-debug-all:jar:5.1:compile, org.jacoco:org.jacoco.report=org.jacoco:org.jacoco.report:jar:0.7.7.201606060606:compile}
[DEBUG]   (f) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (f) skip = false
[DEBUG] -- end configuration --
[INFO] argLine set to -javaagent:C:\\Dev\\Maven\\repository\\org\\jacoco\\org.jacoco.agent\\0.7.7.201606060606\\org.jacoco.agent-0.7.7.201606060606-runtime.jar=destfile=C:\\Dev\\RIVM\\rivm-episim\\java\\epidemes-demo-webapp\\target\\jacoco.exec
[INFO] 
[INFO] <<< maven-source-plugin:3.0.0:jar (attach-sources) < generate-sources @ epidemes-demo-webapp <<<
[INFO] 
[INFO] --- maven-source-plugin:3.0.0:jar (attach-sources) @ epidemes-demo-webapp ---
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=0, ConflictMarker.markTime=0, ConflictMarker.nodeCount=80, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=0, ConflictIdSorter.conflictIdCount=31, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=1, ConflictResolver.conflictItemCount=74, DefaultDependencyCollector.collectTime=32, DefaultDependencyCollector.transformTime=1}
[DEBUG] org.apache.maven.plugins:maven-source-plugin:jar:3.0.0:
[DEBUG]    org.apache.maven:maven-model:jar:3.0:compile
[DEBUG]    org.apache.maven:maven-artifact:jar:3.0:compile
[DEBUG]    org.apache.maven:maven-plugin-api:jar:3.0:compile
[DEBUG]       org.sonatype.sisu:sisu-inject-plexus:jar:1.4.2:compile
[DEBUG]          org.sonatype.sisu:sisu-inject-bean:jar:1.4.2:compile
[DEBUG]             org.sonatype.sisu:sisu-guice:jar:noaop:2.1.7:compile
[DEBUG]    org.apache.maven:maven-core:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-settings:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-settings-builder:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-repository-metadata:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-model-builder:jar:3.0:compile
[DEBUG]       org.apache.maven:maven-aether-provider:jar:3.0:runtime
[DEBUG]       org.sonatype.aether:aether-impl:jar:1.7:compile
[DEBUG]          org.sonatype.aether:aether-spi:jar:1.7:compile
[DEBUG]       org.sonatype.aether:aether-api:jar:1.7:compile
[DEBUG]       org.sonatype.aether:aether-util:jar:1.7:compile
[DEBUG]       org.codehaus.plexus:plexus-interpolation:jar:1.14:compile
[DEBUG]       org.codehaus.plexus:plexus-classworlds:jar:2.2.3:compile
[DEBUG]       org.codehaus.plexus:plexus-component-annotations:jar:1.5.5:compile
[DEBUG]       org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3:compile
[DEBUG]          org.sonatype.plexus:plexus-cipher:jar:1.4:compile
[DEBUG]    org.apache.maven:maven-archiver:jar:3.0.0:compile
[DEBUG]       org.apache.maven.shared:maven-shared-utils:jar:3.0.0:compile
[DEBUG]          commons-io:commons-io:jar:2.4:compile
[DEBUG]          com.google.code.findbugs:jsr305:jar:2.0.1:compile
[DEBUG]    org.codehaus.plexus:plexus-archiver:jar:3.0.3:compile
[DEBUG]       org.codehaus.plexus:plexus-io:jar:2.7:compile
[DEBUG]       org.apache.commons:commons-compress:jar:1.10:compile
[DEBUG]       org.iq80.snappy:snappy:jar:0.4:compile
[DEBUG]    org.codehaus.plexus:plexus-utils:jar:3.0.22:compile
[DEBUG] Created new class realm plugin>org.apache.maven.plugins:maven-source-plugin:3.0.0
[DEBUG] Importing foreign packages into class realm plugin>org.apache.maven.plugins:maven-source-plugin:3.0.0
[DEBUG]   Imported:  < project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Populating class realm plugin>org.apache.maven.plugins:maven-source-plugin:3.0.0
[DEBUG]   Included: org.apache.maven.plugins:maven-source-plugin:jar:3.0.0
[DEBUG]   Included: org.sonatype.sisu:sisu-inject-bean:jar:1.4.2
[DEBUG]   Included: org.sonatype.sisu:sisu-guice:jar:noaop:2.1.7
[DEBUG]   Included: org.sonatype.aether:aether-util:jar:1.7
[DEBUG]   Included: org.codehaus.plexus:plexus-interpolation:jar:1.14
[DEBUG]   Included: org.codehaus.plexus:plexus-component-annotations:jar:1.5.5
[DEBUG]   Included: org.sonatype.plexus:plexus-sec-dispatcher:jar:1.3
[DEBUG]   Included: org.sonatype.plexus:plexus-cipher:jar:1.4
[DEBUG]   Included: org.apache.maven:maven-archiver:jar:3.0.0
[DEBUG]   Included: org.apache.maven.shared:maven-shared-utils:jar:3.0.0
[DEBUG]   Included: commons-io:commons-io:jar:2.4
[DEBUG]   Included: com.google.code.findbugs:jsr305:jar:2.0.1
[DEBUG]   Included: org.codehaus.plexus:plexus-archiver:jar:3.0.3
[DEBUG]   Included: org.codehaus.plexus:plexus-io:jar:2.7
[DEBUG]   Included: org.apache.commons:commons-compress:jar:1.10
[DEBUG]   Included: org.iq80.snappy:snappy:jar:0.4
[DEBUG]   Included: org.codehaus.plexus:plexus-utils:jar:3.0.22
[DEBUG] Configuring mojo org.apache.maven.plugins:maven-source-plugin:3.0.0:jar from plugin realm ClassRealm[plugin>org.apache.maven.plugins:maven-source-plugin:3.0.0, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.apache.maven.plugins:maven-source-plugin:3.0.0:jar' with basic configurator -->
[DEBUG]   (f) attach = true
[DEBUG]   (f) classifier = sources
[DEBUG]   (f) defaultManifestFile = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\classes\META-INF\MANIFEST.MF
[DEBUG]   (f) excludeResources = false
[DEBUG]   (f) finalName = epidemes-demo-webapp-0.1.0-SNAPSHOT
[DEBUG]   (f) forceCreation = false
[DEBUG]   (f) includePom = false
[DEBUG]   (f) outputDirectory = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target
[DEBUG]   (f) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (f) reactorProjects = [MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml]
[DEBUG]   (f) session = org.apache.maven.execution.MavenSession@773bd77b
[DEBUG]   (f) skipSource = false
[DEBUG]   (f) useDefaultExcludes = true
[DEBUG]   (f) useDefaultManifestFile = false
[DEBUG] -- end configuration --
[INFO] No sources in project. Archive not created.
[INFO] 
[INFO] --- cargo-maven2-plugin:1.6.2:start (start-cargo) @ epidemes-demo-webapp ---
[DEBUG] Dependency collection stats: {ConflictMarker.analyzeTime=0, ConflictMarker.markTime=0, ConflictMarker.nodeCount=31, ConflictIdSorter.graphTime=0, ConflictIdSorter.topsortTime=0, ConflictIdSorter.conflictIdCount=24, ConflictIdSorter.conflictIdCycleCount=0, ConflictResolver.totalTime=1, ConflictResolver.conflictItemCount=31, DefaultDependencyCollector.collectTime=8, DefaultDependencyCollector.transformTime=1}
[DEBUG] org.codehaus.cargo:cargo-maven2-plugin:jar:1.6.2:
[DEBUG]    org.apache.maven:maven-archiver:jar:2.4.1:compile
[DEBUG]       org.apache.maven:maven-artifact:jar:2.0.6:compile
[DEBUG]       org.apache.maven:maven-model:jar:2.0.6:compile
[DEBUG]       org.codehaus.plexus:plexus-archiver:jar:1.0:compile
[DEBUG]          org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-1:compile
[DEBUG]             junit:junit:jar:3.8.2:compile
[DEBUG]             classworlds:classworlds:jar:1.1-alpha-2:compile
[DEBUG]          org.codehaus.plexus:plexus-io:jar:1.0:compile
[DEBUG]       org.codehaus.plexus:plexus-utils:jar:2.0.0:compile
[DEBUG]       org.codehaus.plexus:plexus-interpolation:jar:1.13:compile
[DEBUG]    org.codehaus.cargo:cargo-core-api-generic:jar:1.6.2:compile
[DEBUG]       commons-discovery:commons-discovery:jar:0.5:compile
[DEBUG]          commons-logging:commons-logging:jar:1.1.1:compile
[DEBUG]       org.codehaus.cargo:cargo-core-api-container:jar:1.6.2:compile
[DEBUG]          org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:compile
[DEBUG]             jaxen:jaxen:jar:1.1.6:compile
[DEBUG]             org.jdom:jdom:jar:1.1.3:compile
[DEBUG]          org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:compile
[DEBUG]    org.codehaus.cargo:cargo-documentation:jar:1.6.2:compile
[DEBUG]    org.codehaus.cargo:cargo-daemon-client:jar:1.6.2:compile
[DEBUG]       org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:compile
[DEBUG]          org.apache.ant:ant:jar:1.7.1:compile
[DEBUG]             org.apache.ant:ant-launcher:jar:1.7.1:compile
[DEBUG] Created new class realm plugin>org.codehaus.cargo:cargo-maven2-plugin:1.6.2
[DEBUG] Importing foreign packages into class realm plugin>org.codehaus.cargo:cargo-maven2-plugin:1.6.2
[DEBUG]   Imported:  < project>nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT
[DEBUG] Populating class realm plugin>org.codehaus.cargo:cargo-maven2-plugin:1.6.2
[DEBUG]   Included: org.codehaus.cargo:cargo-maven2-plugin:jar:1.6.2
[DEBUG]   Included: org.apache.maven:maven-archiver:jar:2.4.1
[DEBUG]   Included: org.codehaus.plexus:plexus-archiver:jar:1.0
[DEBUG]   Included: junit:junit:jar:3.8.2
[DEBUG]   Included: org.codehaus.plexus:plexus-io:jar:1.0
[DEBUG]   Included: org.codehaus.plexus:plexus-utils:jar:2.0.0
[DEBUG]   Included: org.codehaus.plexus:plexus-interpolation:jar:1.13
[DEBUG]   Included: org.codehaus.cargo:cargo-core-api-generic:jar:1.6.2
[DEBUG]   Included: commons-discovery:commons-discovery:jar:0.5
[DEBUG]   Included: commons-logging:commons-logging:jar:1.1.1
[DEBUG]   Included: org.codehaus.cargo:cargo-core-api-container:jar:1.6.2
[DEBUG]   Included: org.codehaus.cargo:cargo-core-api-module:jar:1.6.2
[DEBUG]   Included: jaxen:jaxen:jar:1.1.6
[DEBUG]   Included: org.jdom:jdom:jar:1.1.3
[DEBUG]   Included: org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1
[DEBUG]   Included: org.codehaus.cargo:cargo-documentation:jar:1.6.2
[DEBUG]   Included: org.codehaus.cargo:cargo-daemon-client:jar:1.6.2
[DEBUG]   Included: org.codehaus.cargo:cargo-core-api-util:jar:1.6.2
[DEBUG]   Included: org.apache.ant:ant:jar:1.7.1
[DEBUG]   Included: org.apache.ant:ant-launcher:jar:1.7.1
[DEBUG] Configuring mojo org.codehaus.cargo:cargo-maven2-plugin:1.6.2:start from plugin realm ClassRealm[plugin>org.codehaus.cargo:cargo-maven2-plugin:1.6.2, parent: sun.misc.Launcher$AppClassLoader@55f96302]
[DEBUG] Configuring mojo 'org.codehaus.cargo:cargo-maven2-plugin:1.6.2:start' with basic configurator -->
[DEBUG]   (s) containerId = wildfly10x
[DEBUG]   (s) url = http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip
[DEBUG]   (s) downloadDir = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.cargo/downloads
[DEBUG]   (s) extractDir = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp/.cargo/extracts
[DEBUG]   (s) zipUrlInstaller = org.codehaus.cargo.maven2.configuration.ZipUrlInstaller@59939293
[DEBUG]   (s) log = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\cargo.log
[DEBUG]   (s) output = C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target/wildfly.log
[DEBUG]   (s) systemProperties = {}
[DEBUG]   (f) container = org.codehaus.cargo.maven2.configuration.Container@68b366e2
[DEBUG]   (s) properties = {context=epidemes}
[DEBUG]   (f) pingTimeout = 120000
[DEBUG]   (f) deployables = [AbstractDependency{ groupId=null, artifactId=null, type=null, classifier=null }]
[DEBUG]   (s) ignoreFailures = false
[DEBUG]   (f) localRepository =       id: local
      url: file:///C:/Dev/Maven/repository/
   layout: default
snapshots: [enabled => true, update => always]
 releases: [enabled => true, update => always]

[DEBUG]   (f) pluginVersion = 1.6.2
[DEBUG]   (f) project = MavenProject: nl.rivm.cib:epidemes-demo-webapp:0.1.0-SNAPSHOT @ C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\pom.xml
[DEBUG]   (f) repositories = [      id: central
      url: https://repo.maven.apache.org/maven2
   layout: default
snapshots: [enabled => false, update => daily]
 releases: [enabled => true, update => daily]
]
[DEBUG]   (f) settings = org.apache.maven.execution.SettingsAdapter@78d71df1
[DEBUG]   (f) skip = false
[DEBUG] -- end configuration --
[DEBUG] dummy:dummy:pom:0.1 (selected for null)
[DEBUG]   org.codehaus.cargo:cargo-core-container-wildfly:jar:1.6.2 (selected for null)
[DEBUG]     org.codehaus.cargo:cargo-core-container-jboss:jar:1.6.2:provided (selected for provided)
[DEBUG]       org.codehaus.cargo:cargo-core-api-generic:jar:1.6.2:provided (selected for provided)
[DEBUG]         commons-discovery:commons-discovery:jar:0.5:provided (selected for provided)
[DEBUG]           commons-logging:commons-logging:jar:1.1.1:provided (selected for provided)
[DEBUG]         org.codehaus.cargo:cargo-core-api-container:jar:1.6.2:provided (selected for provided)
[DEBUG]           org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:provided (selected for provided)
[DEBUG]             jaxen:jaxen:jar:1.1.6:provided (selected for provided)
[DEBUG]             org.jdom:jdom:jar:1.1.3:provided (selected for provided)
[DEBUG]             org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:provided (selected for provided)
[DEBUG]               org.apache.ant:ant:jar:1.7.1:provided (selected for provided)
[DEBUG]                 org.apache.ant:ant-launcher:jar:1.7.1:provided (selected for provided)
[DEBUG]           org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:provided (selected for provided)
[DEBUG]       commons-discovery:commons-discovery:jar:0.5:provided (selected for provided)
[DEBUG]         commons-logging:commons-logging:jar:1.1.1:provided (selected for provided)
[DEBUG]       org.codehaus.cargo:cargo-core-api-container:jar:1.6.2:provided (selected for provided)
[DEBUG]         org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:provided (selected for provided)
[DEBUG]           jaxen:jaxen:jar:1.1.6:provided (selected for provided)
[DEBUG]           org.jdom:jdom:jar:1.1.3:provided (selected for provided)
[DEBUG]           org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:provided (selected for provided)
[DEBUG]             org.apache.ant:ant:jar:1.7.1:provided (selected for provided)
[DEBUG]               org.apache.ant:ant-launcher:jar:1.7.1:provided (selected for provided)
[DEBUG]         org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:provided (selected for provided)
[DEBUG]       org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:provided (selected for provided)
[DEBUG]         jaxen:jaxen:jar:1.1.6:provided (selected for provided)
[DEBUG]         org.jdom:jdom:jar:1.1.3:provided (selected for provided)
[DEBUG]         org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:provided (selected for provided)
[DEBUG]           org.apache.ant:ant:jar:1.7.1:provided (selected for provided)
[DEBUG]             org.apache.ant:ant-launcher:jar:1.7.1:provided (selected for provided)
[DEBUG]       jaxen:jaxen:jar:1.1.6:provided (selected for provided)
[DEBUG]       org.jdom:jdom:jar:1.1.3:provided (selected for provided)
[DEBUG]       org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:provided (selected for provided)
[DEBUG]         org.apache.ant:ant:jar:1.7.1:provided (selected for provided)
[DEBUG]           org.apache.ant:ant-launcher:jar:1.7.1:provided (selected for provided)
[DEBUG]       org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:provided (selected for provided)
[DEBUG]       org.apache.ant:ant:jar:1.7.1:provided (selected for provided)
[DEBUG]         org.apache.ant:ant-launcher:jar:1.7.1:provided (selected for provided)
[DEBUG]       org.apache.ant:ant-launcher:jar:1.7.1:provided (selected for provided)
[DEBUG]       commons-logging:commons-logging:jar:1.1.1:provided (selected for provided)
[DEBUG]     com.googlecode.json-simple:json-simple:jar:1.1.1:compile (selected for compile)
[DEBUG]       junit:junit:jar:4.10:compile (selected for compile)
[DEBUG]         org.hamcrest:hamcrest-core:jar:1.1:compile (selected for compile)
[DEBUG]     org.codehaus.cargo:cargo-core-api-generic:jar:1.6.2:compile (selected for compile)
[DEBUG]       commons-discovery:commons-discovery:jar:0.5:provided (setting artifactScope to: compile)
[DEBUG]       commons-discovery:commons-discovery:jar:0.5:compile (selected for compile)
[DEBUG]         commons-logging:commons-logging:jar:1.1.1:provided (setting artifactScope to: compile)
[DEBUG]         commons-logging:commons-logging:jar:1.1.1:compile (selected for compile)
[DEBUG]       org.codehaus.cargo:cargo-core-api-container:jar:1.6.2:provided (setting artifactScope to: compile)
[DEBUG]       org.codehaus.cargo:cargo-core-api-container:jar:1.6.2:compile (selected for compile)
[DEBUG]         org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:provided (setting artifactScope to: compile)
[DEBUG]         org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:compile (selected for compile)
[DEBUG]           jaxen:jaxen:jar:1.1.6:provided (setting artifactScope to: compile)
[DEBUG]           jaxen:jaxen:jar:1.1.6:compile (selected for compile)
[DEBUG]           org.jdom:jdom:jar:1.1.3:provided (setting artifactScope to: compile)
[DEBUG]           org.jdom:jdom:jar:1.1.3:compile (selected for compile)
[DEBUG]           org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:provided (setting artifactScope to: compile)
[DEBUG]           org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:compile (selected for compile)
[DEBUG]             org.apache.ant:ant:jar:1.7.1:provided (setting artifactScope to: compile)
[DEBUG]             org.apache.ant:ant:jar:1.7.1:compile (selected for compile)
[DEBUG]               org.apache.ant:ant-launcher:jar:1.7.1:provided (setting artifactScope to: compile)
[DEBUG]               org.apache.ant:ant-launcher:jar:1.7.1:compile (selected for compile)
[DEBUG]         org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:provided (setting artifactScope to: compile)
[DEBUG]         org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:compile (selected for compile)
[DEBUG]     xmlunit:xmlunit:jar:1.2:test (selected for test)
[DEBUG]     org.codehaus.cargo:cargo-core-api-container:jar:1.6.2:compile (selected for compile)
[DEBUG]       org.codehaus.cargo:cargo-core-api-module:jar:1.6.2:compile (selected for compile)
[DEBUG]         jaxen:jaxen:jar:1.1.6:compile (selected for compile)
[DEBUG]         org.jdom:jdom:jar:1.1.3:compile (selected for compile)
[DEBUG]         org.codehaus.cargo:cargo-core-api-util:jar:1.6.2:compile (selected for compile)
[DEBUG]           org.apache.ant:ant:jar:1.7.1:compile (selected for compile)
[DEBUG]             org.apache.ant:ant-launcher:jar:1.7.1:compile (selected for compile)
[DEBUG]       org.apache.geronimo.specs:geronimo-j2ee-deployment_1.1_spec:jar:1.1:compile (selected for compile)
[DEBUG]     org.codehaus.cargo:cargo-core-api-container:test-jar:tests:1.6.2:test (selected for test)
[DEBUG]     org.codehaus.cargo:cargo-core-api-util:test-jar:tests:1.6.2:test (selected for test)
[DEBUG]       org.apache.ant:ant:jar:1.7.1:test (setting artifactScope to: compile)
[DEBUG]     junit:junit:jar:3.8.2:test (setting artifactScope to: compile)
[DEBUG]     jmock:jmock:jar:1.2.0:test (selected for test)
[DEBUG]       junit:junit:jar:3.8.1:test (removed - nearer found: 3.8.2)
[DEBUG]     jmock:jmock-cglib:jar:1.2.0:test (selected for test)
[DEBUG]       cglib:cglib-nodep:jar:2.1_3:test (selected for test)
[DEBUG]     commons-vfs:commons-vfs:jar:1.0:test (selected for test)
[DEBUG]       commons-logging:commons-logging:jar:1.0.4:test (setting artifactScope to: compile)
[DEBUG] Scheduling deployable for deployment: [groupId [null], artifactId [null], type [null], location [null], pingURL [null]]
[DEBUG] Initial deployable values: groupId = [null], artifactId = [null], type = [null], location = [null]
[DEBUG] Searching for an artifact that matches [nl.rivm.cib:epidemes-demo-webapp:war:null]...
[DEBUG] Checking artifact [nl.rivm.cib:epidemes-demo-webapp:war:null]...
[DEBUG] Computed deployable values: groupId = [nl.rivm.cib], artifactId = [epidemes-demo-webapp], classifier = [null], type = [war], location = [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT.war]
[DEBUG] Setting deployable property [context]:[epidemes] for [C:\Dev\RIVM\rivm-episim\java\epidemes-demo-webapp\target\epidemes-demo-webapp-0.1.0-SNAPSHOT.war]
[DEBUG] Invoking setter method public synchronized void org.codehaus.cargo.container.deployable.WAR.setContext(java.lang.String) for deployable org.codehaus.cargo.container.jboss.deployable.JBossWAR[epidemes-demo-webapp-0.1.0-SNAPSHOT.war] with argument epidemes
Terminate batch job (Y/N)? 
Terminate batch job (Y/N)? 
