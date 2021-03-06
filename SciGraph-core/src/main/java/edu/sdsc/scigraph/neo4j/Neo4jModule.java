/**
 * Copyright (C) 2014 The SciGraph authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sdsc.scigraph.neo4j;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Named;
import javax.inject.Singleton;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

import edu.sdsc.scigraph.frames.Concept;
import edu.sdsc.scigraph.neo4j.bindings.IndicatesNeo4j;
import edu.sdsc.scigraph.vocabulary.Vocabulary;
import edu.sdsc.scigraph.vocabulary.VocabularyNeo4jImpl;

public class Neo4jModule extends AbstractModule {

  private Optional<String> graphLocation = Optional.absent();

  /***
   * @deprecated Configuration should now be done with yaml files.
   */
  @Deprecated
  public Neo4jModule() {}

  public Neo4jModule(OntologyConfiguration configuration) {
    this.graphLocation = Optional.of(configuration.getGraphLocation());
  }

  @Override
  protected void configure() {
    if (!graphLocation.isPresent()) {
      Properties properties = loadProperties(this, "neo4j.properties");
      Names.bindProperties(binder(), properties);
    } else {
      bind(String.class).annotatedWith(Names.named("neo4j.location")).toInstance(graphLocation.get());
    }
    bind(new TypeLiteral<Class<?>>() {}).toInstance(Concept.class);
    TransactionalInterceptor interceptor = new TransactionalInterceptor();
    requestInjection(interceptor);
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), 
        interceptor);
  }

  @Provides
  @Singleton
  Vocabulary<Concept> getVocabulary(Graph<Concept> graph, @Named("neo4j.location") String graphLocation) throws IOException {
    return new VocabularyNeo4jImpl<Concept>(graph, graphLocation);
  }

  @Provides
  @Singleton
  @IndicatesNeo4j
  AtomicBoolean getInTransaction() {
    return new AtomicBoolean();
  }

  @Provides
  @Singleton
  GraphDatabaseService getGraphDatabaseService(@Named("neo4j.location") String neo4jLocation) throws IOException {
    try {
      Map<String, String> config = new HashMap<>();
      config.put("neostore.nodestore.db.mapped_memory", "500M");
      config.put("neostore.relationshipstore.db.mapped_memory", "500M");
      config.put("neostore.propertystore.db.mapped_memory", "500M");
      config.put("neostore.propertystore.db.strings.mapped_memory", "500M");
      config.put("neostore.propertystore.db.arrays.mapped_memory", "500M");
      final GraphDatabaseService graphDb = new GraphDatabaseFactory()
      .newEmbeddedDatabaseBuilder(neo4jLocation)
      .setConfig(config)
      .newGraphDatabase();

      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() { graphDb.shutdown(); }
      });
      return graphDb;
    } catch (Exception e) {
      if (Throwables.getRootCause(e).getMessage().contains("lock file")) {
        throw new IOException(format("The graph at \"%s\" is locked by another process", neo4jLocation));
      }
      throw e;
    }
  }

  @Deprecated
  private static Properties loadProperties(Object object, String name) {
    Properties properties = new Properties();
    try (InputStream is = object.getClass().getResourceAsStream(name)) {
      properties.load(is);
    } catch (Exception e) {
      try (InputStream is = object.getClass().getResourceAsStream("/" + name)) {
        properties.load(is);
      } catch (Exception ex) {
      }
    } 
    return properties;
  }

}
