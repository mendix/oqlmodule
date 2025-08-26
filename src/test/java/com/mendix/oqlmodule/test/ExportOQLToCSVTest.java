package com.mendix.oqlmodule.test;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import oql.actions.ExportOQLToCSV;
import oql.proxies.CSVDownload;
import oql.proxies.ExamplePerson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExportOQLToCSVTest extends OQLStatementTestSkeleton {
  final static String DEFAULT_SEPARATOR = ",";
  final static String DEFAULT_QUOTE = "'";
  final static String EXPECTED_CSV_NAME = "ExportedExamplePersons.csv";

  String expectedCSV;

  @BeforeAll
  public void loadExpectedFile() {
    String basePath = Core.getConfiguration().getResourcesPath().getAbsolutePath();

    String fileContents;
    try (InputStream inputStream = new FileInputStream(Paths.get(basePath).resolve(EXPECTED_CSV_NAME).toFile())) {
      fileContents = new String(inputStream.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    int i = 1;
    for (ExamplePerson person : this.testPersons) {
      String templateId = "ExamplePersonID" + i;
      String objectId = ((Long) person.getMendixObject().getId().toLong()).toString();
      fileContents = fileContents.replace(templateId, objectId);
      i++;
    }

    this.expectedCSV = fileContents;
  }

  @AfterEach
  public void cleanUpFiles() {
    Core.delete(this.context, Core.createXPathQuery("//OQL.CSVDownload").execute(this.context));
  }

  @Test
  public void exportOQLToCSV() throws Exception {
    IMendixObject returnObject =
      new ExportOQLToCSV(this.context, selectSome, CSVDownload.entityName, false, false, true, DEFAULT_SEPARATOR, DEFAULT_QUOTE, null)
        .executeAction();

    assertEquals(this.expectedCSV, fileContents(returnObject));
  }

  @Test
  public void exportOQLToCSVZipResults() throws Exception {
    // Load data as zip
    IMendixObject returnObjectZipped =
      new ExportOQLToCSV(this.context, selectSome, CSVDownload.entityName, false, true, true, DEFAULT_SEPARATOR, DEFAULT_QUOTE, null)
        .executeAction();

    // Unzip the results
    String contentsZipped;
    try (InputStream stream = Core.getFileDocumentContent(this.context, returnObjectZipped);
         ZipInputStream unzipped = new ZipInputStream(stream)) {
      unzipped.getNextEntry();
      contentsZipped = new String(unzipped.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Load data without zipping
    IMendixObject returnObject =
      new ExportOQLToCSV(this.context, selectSome, CSVDownload.entityName, false, false, true, DEFAULT_SEPARATOR, DEFAULT_QUOTE, null)
        .executeAction();

    String contents = fileContents(returnObject);

    // Compare results
    assertEquals(contents, contentsZipped);
  }

  @Test
  public void exportOQLToCSVRemoveHeader() throws Exception {
    IMendixObject returnObject =
      new ExportOQLToCSV(this.context, selectSome, CSVDownload.entityName, false, false, false, DEFAULT_SEPARATOR, DEFAULT_QUOTE, null)
        .executeAction();

    assertEquals(this.expectedCSV.substring(this.expectedCSV.indexOf("\r\n") + 2), fileContents(returnObject));
  }

  @Test
  public void exportOQLToCSVDifferentSeparator() throws Exception {
    IMendixObject returnObject =
      new ExportOQLToCSV(this.context, selectSome, CSVDownload.entityName, false, false, true, "@", DEFAULT_QUOTE, null)
        .executeAction();

    assertEquals(this.expectedCSV.replace(",", "@"), fileContents(returnObject));
  }

  @Test
  public void exportOQLToCSVDifferentQuote() throws Exception {
    IMendixObject returnObject =
      new ExportOQLToCSV(this.context, selectSome, CSVDownload.entityName, false, false, true, DEFAULT_SEPARATOR, "@", null)
        .executeAction();

    assertEquals(this.expectedCSV.replace("'", "@"), fileContents(returnObject));
  }

  @Test
  public void exportOQLToCSVDifferentEscape() throws Exception {
    IMendixObject returnObject =
      new ExportOQLToCSV(this.context, selectSome, CSVDownload.entityName, false, false, true, DEFAULT_SEPARATOR, null, "\\")
        .executeAction();

    assertEquals(this.expectedCSV.replace("\r\n\\", "\r\\\n\\\\").replace("'", ""), fileContents(returnObject));
  }

  @Test
  public void exportOQLToCSVRemoveCRLF() throws Exception {
    exportOQLToCSVRemoveNewLines("\r\n\r\n\\", 2);
  }

  @Test
  public void exportOQLToCSVRemoveLF() throws Exception {
    exportOQLToCSVRemoveNewLines("\n\n\\", 2);
  }

  @Test
  public void exportOQLToCSVRemoveCR() throws Exception {
    // Just in case someone imports data from vintage (classic Mac OS, Commodore) systems, perhaps?
    exportOQLToCSVRemoveNewLines("\r\r\\", 2);
  }

  @Test
  public void exportOQLToCSVRemoveLFCRLF() throws Exception {
    exportOQLToCSVRemoveNewLines("\n\r\n\\", 2);
  }

  @Test
  public void exportOQLToCSVRemoveNewLineCombinations() throws Exception {
    exportOQLToCSVRemoveNewLines("\r\n\r\n\n\n\r\r\\", 6);
  }

  private void exportOQLToCSVRemoveNewLines(String newLineInObject, int spaces) throws Exception {
    // Replace the new line sequence to test various combinations
    String nameField = ExamplePerson.MemberNames.Name.toString();

    List<IMendixObject> objects = Core.createXPathQuery("//OQL.ExamplePerson").execute(this.context);
    objects.forEach(obj ->
      obj.setValue(this.context, nameField, ((String) obj.getValue(this.context, nameField)).replace("\r\n\\", newLineInObject))
    );
    Core.commit(this.context, objects);

    // Then test
    IMendixObject returnObject =
      new ExportOQLToCSV(this.context, selectSome, CSVDownload.entityName, true, false, true, DEFAULT_SEPARATOR, DEFAULT_QUOTE, null)
        .executeAction();

    assertEquals(this.expectedCSV.replace("\r\n\\", " ".repeat(spaces) + "\\"), fileContents(returnObject));
  }

  private String fileContents(IMendixObject object) {
    try (InputStream stream = Core.getFileDocumentContent(this.context, object)) {
      return new String(stream.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
