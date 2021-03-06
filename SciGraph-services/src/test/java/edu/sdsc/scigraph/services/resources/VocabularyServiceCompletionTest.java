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
package edu.sdsc.scigraph.services.resources;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.sdsc.scigraph.frames.Concept;
import edu.sdsc.scigraph.vocabulary.Vocabulary;
import edu.sdsc.scigraph.vocabulary.Vocabulary.Query;

public class VocabularyServiceCompletionTest {

  Concept result;

  @Before
  public void setup() {
    result = mock(Concept.class);
    when(result.getLabels()).thenReturn(newArrayList("Hippocampus"));
    when(result.getSynonyms()).thenReturn(newArrayList("Amon's horn", "Hippocampal region"));
  }

  @Test
  public void testLabelCompletion() {
    Query query = new Vocabulary.Query.Builder("Hippo").includeSynonyms(false).build();
    List<String> completionStrings = VocabularyService.getCompletion(query, result);
    assertThat(completionStrings, contains("Hippocampus"));
  }

  @Test
  public void testLabelCompletionIgnoresCase() {
    Query query = new Vocabulary.Query.Builder("hippo").includeSynonyms(false).build();
    List<String> completionStrings = VocabularyService.getCompletion(query, result);
    assertThat(completionStrings, contains("Hippocampus"));
  }

  @Test
  public void testSynonymCompletion() {
    Query query = new Vocabulary.Query.Builder("amon").build();
    List<String> completionStrings = VocabularyService.getCompletion(query, result);
    assertThat(completionStrings, contains("Amon's horn"));
  }

  @Test
  public void testMultiplMatches() {
    Query query = new Vocabulary.Query.Builder("Hippo").build();
    List<String> completionStrings = VocabularyService.getCompletion(query, result);
    assertThat(completionStrings, contains("Hippocampus", "Hippocampal region"));
  }

}
