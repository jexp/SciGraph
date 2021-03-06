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
package edu.sdsc.scigraph.owlapi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import au.com.bytecode.opencsv.CSVReader;

public class Csv2Owl { 

  OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

  OWLDataFactory df = OWLManager.getOWLDataFactory(); 

  public OWLOntologyManager getManager() {
    return manager;
  }

  OWLClass addClass(String iri) {
    return df.getOWLClass(IRI.create(iri));
  }

  OWLOntology convert(String ontologyIri, Reader reader, String parentIri) throws FileNotFoundException, IOException, OWLOntologyCreationException {
    OWLOntology ontology = manager.createOntology(IRI.create(ontologyIri));
    OWLClass parent = df.getOWLClass(IRI.create(parentIri));
    manager.applyChange(new AddAxiom(ontology, df.getOWLDeclarationAxiom(parent)));
    try (CSVReader csvReader = new CSVReader(reader, '\t')) {
      String[] columns = null;
      while ((columns = csvReader.readNext()) != null) {
        OWLClass concept = df.getOWLClass(IRI.create(ontologyIri + "#" + columns[0]));
        manager.applyChange(new AddAxiom(ontology, df.getOWLDeclarationAxiom(concept)));
        OWLAnnotation labelAnnotation = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(columns[1]));
        OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(concept.getIRI(), labelAnnotation);
        manager.applyChange(new AddAxiom(ontology, axiom));
        axiom = df.getOWLSubClassOfAxiom(concept, parent);
        manager.applyChange(new AddAxiom(ontology, axiom));
      }
    }
    return ontology;
  }

  public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, IOException, OWLOntologyStorageException {
    Csv2Owl converter = new Csv2Owl();
    OWLOntology ontology = converter.convert("http://earthcube.org/ships", new FileReader("/temp/nodc_by_name.tsv"), "http://earthcube.org/ships");
    converter.getManager().saveOntology(ontology, IRI.create(new File("/temp/shipNames.owl")));
  }

}
