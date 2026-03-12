package com.mendix.oqlmodule.test;

import oql.actions.*;
import oqlexample.proxies.ExampleData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecuteDMLStatementWithParametersTest extends OQLStatementTestSkeleton {

  @Test
  public void executeDMLWithBooleanParameter() throws Exception {
    new AddBooleanParameter(this.context, "Param", true).executeAction();

    Long result = new ExecuteDMLStatement(
      this.context,
      "UPDATE OQL.ExamplePerson SET Active = $Param WHERE Name = 'Example\r\n\\Person 1'"
    ).executeAction();

    assertEquals(1L, result);
  }

  @Test
  public void executeDMLWithDecimalParameter() throws Exception {
    new AddDecimalParameter(this.context, "Param", new BigDecimal(170)).executeAction();

    Long result = new ExecuteDMLStatement(
      this.context,
      "UPDATE OQL.ExamplePerson SET Active = true WHERE HeightInDecimal > $Param"
    ).executeAction();

    assertEquals(4L, result);
  }

  @Test
  public void executeDMLWithStringParameter() throws Exception {
    new AddStringParameter(this.context, "Param", "Example\r\n\\Person 1").executeAction();

    Long result = new ExecuteDMLStatement(
      this.context,
      "UPDATE OQL.ExamplePerson SET Active = true WHERE Name = $Param"
    ).executeAction();

    assertEquals(1L, result);
  }

  @Test
  public void executeDMLWithLongParameter() throws Exception {
    new AddIntegerLongValue(this.context, "Param", 22L).executeAction();

    Long result = new ExecuteDMLStatement(
      this.context,
      "UPDATE OQL.ExamplePerson SET LongAge = $Param WHERE Name = 'Example\r\n\\Person 1'"
    ).executeAction();

    assertEquals(1L, result);
  }

  @Test
  public void executeDMLWithIntegerParameter() throws Exception {
    new AddIntegerLongValue(this.context, "Param", 30L).executeAction();

    Long result = new ExecuteDMLStatement(
      this.context,
      "UPDATE OQL.ExamplePerson SET Age = $Param WHERE Name = 'Example\r\n\\Person 1'"
    ).executeAction();

    assertEquals(1L, result);
  }

  private ExampleData createExample(String contents, Long counter) throws Exception {
    ExampleData d = new ExampleData(this.context);
    d.setContents(contents);
    d.commit();
    return d;
  }
}
