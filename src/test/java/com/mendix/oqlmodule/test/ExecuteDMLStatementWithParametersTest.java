package com.mendix.oqlmodule.test;

import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import oql.actions.*;
import oql.proxies.ExamplePerson;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ExecuteDMLStatementWithParametersTest extends OQLStatementTestSkeleton {

  @Test
  public void executeDMLWithoutParameters() {
    assertThrows(MendixRuntimeException.class, () ->
      new ExecuteDMLStatement(this.context, "UPDATE OQLExample.ExampleData SET Contents = $Param WHERE Contents = 'ToDelete'").executeAction()
    );
  }

  @Test
  public void executeDMLWithNullParameter() throws Exception {
    new AddBooleanParameter(this.context, "Param", null).executeAction();

    assertThrows(IllegalArgumentException.class, () ->
      new ExecuteDMLStatement(this.context, "DELETE FROM OQL.ExamplePerson WHERE Active = $Param").executeAction()
    );
  }

  @Test
  public void executeDMLWithMultipleAffectedRows() throws Exception {
    new AddBooleanParameter(this.context, "Param", false).executeAction();

    Long affectedRows = new ExecuteDMLStatement(this.context, "DELETE FROM OQL.ExamplePerson WHERE Active = $Param").executeAction();
    assertEquals(2, affectedRows);
  }

  @Test
  public void executeDMLWithMultipleParameters() throws Exception {
    new AddBooleanParameter(this.context, "Param1", false).executeAction();
    new AddIntegerLongValue(this.context, "Param2", 3L).executeAction();

    Long affectedRows = new ExecuteDMLStatement(this.context, "DELETE FROM OQL.ExamplePerson WHERE Active = $Param1 OR Age > $Param2").executeAction();
    assertEquals(3, affectedRows);
  }

  @Test
  public void executeDMLWithBooleanParameter() throws Exception {
    new AddBooleanParameter(this.context, "Param", true).executeAction();

    testDMLWithOneParameter("Active", true);
  }

  @Test
  public void executeDMLWithDateTimeParameter() throws Exception {
    new AddDateTimeParameter(this.context, "Param", new Date(System.currentTimeMillis())).executeAction();

    assertThrows(IllegalArgumentException.class, () ->
      new ExecuteDMLStatement(this.context, "UPDATE OQLExample.ExamplePerson SET DateOfBirth = $Param").executeAction()
    );
  }

  @Test
  public void executeDMLWithDecimalParameter() throws Exception {
    BigDecimal decimal = new BigDecimal("170.50000000");
    new AddDecimalParameter(this.context, "Param", decimal).executeAction();

    testDMLWithOneParameter("HeightInDecimal", decimal);
  }

  @Test
  public void executeDMLWithIntegerParameter() throws Exception {
    new AddIntegerLongValue(this.context, "Param", 30L).executeAction();

    testDMLWithOneParameter("Age", 30);
  }

  @Test
  public void executeDMLWithLongParameter() throws Exception {
    new AddIntegerLongValue(this.context, "Param", 22L).executeAction();

    testDMLWithOneParameter("LongAge", 22L);
  }

  @Test
  public void executeDMLWithObjectParameter() throws Exception {
    ExamplePerson person = ExamplePerson.load(this.context, "[Age = 2]").getFirst();
    new AddObjectParameter(this.context, "Param", person.getMendixObject()).executeAction();

    String query = "DELETE FROM OQL.ExamplePerson WHERE OQL.MarriedTo = $Param";
    Long affectedRows = new ExecuteDMLStatement(this.context, query).executeAction();
    assertEquals(1L, affectedRows);
  }

  @Test
  public void executeDMLWithStringParameter() throws Exception {
    new AddStringParameter(this.context, "Param", "New name").executeAction();

    testDMLWithOneParameter("Name", "New name");
  }

  private <T> void testDMLWithOneParameter(String attributeName, T expectedValue) throws Exception {
    ExamplePerson person = ExamplePerson.load(this.context, "[Age = 1]").getFirst();
    IMendixIdentifier personId = person.getMendixObject().getId();

    // Check before testing
    assertAttributeValue(personId, attributeName, expectedValue, false);

    String query = "UPDATE OQL.ExamplePerson SET " + attributeName + " = $Param WHERE id = " + personId.toLong();
    Long affectedRows = new ExecuteDMLStatement(this.context, query).executeAction();

    // Assert the changes were made
    assertEquals(1L, affectedRows);
    assertAttributeValue(personId, attributeName, expectedValue, true);
  }

  private <T> void assertAttributeValue(IMendixIdentifier personId, String attributeName, T expectedValue, boolean equal) throws Exception {
    ExamplePerson updatedPerson = ExamplePerson.load(this.context, personId);
    T actualValue = updatedPerson.getMendixObject().getValue(this.context, attributeName);
    if (equal) {
      assertEquals(expectedValue, actualValue);
    } else {
      assertNotEquals(expectedValue, actualValue);
    }
  }
}
