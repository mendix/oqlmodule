package com.mendix.oqlmodule.test;

import com.mendix.systemwideinterfaces.core.IMendixObject;
import oql.actions.*;
import oql.proxies.ExamplePerson;
import oql.proxies.ExamplePersonResult;
import oql.proxies.Gender;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

public class ExecuteOQLStatementWithParametersTest extends OQLStatementTestSkeleton {

  @Test
  public void executeOQLStatementWithBooleanParameter() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE Active = $Param";
    new AddBooleanParameter(this.context, "Param", true).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, 1000L, 0L, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons.stream().filter(p -> p.getActive(this.context)).collect(Collectors.toList()), oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithBooleanDateTimeParameter() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE DateOfBirth = $Param";
    new AddDateTimeParameter(this.context, "Param", new GregorianCalendar(100, GregorianCalendar.FEBRUARY, 2).getTime()).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons.subList(1, 2), oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithDecimalParameter() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE HeightInDecimal < $Param";
    new AddDecimalParameter(this.context, "Param", new BigDecimal(172)).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons.subList(0, 2), oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithLongParameter() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE LongAge >= $Param";
    new AddIntegerLongValue(this.context, "Param", 21L).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons.subList(1, TEST_OBJECTS), oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithIntegerParameter() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE Age >= $Param";
    new AddIntegerLongValue(this.context, "Param", 1L).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons.subList(1, TEST_OBJECTS), oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithObjectParameter() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE id = $Param";
    new AddObjectParameter(this.context, "Param", this.testPersons.getFirst().getMendixObject()).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons.subList(0, 1), oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithStringParameter() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE Gender = $Param";
    new AddStringParameter(this.context, "Param", Gender.Male.toString()).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    List<ExamplePerson> expectedPersons = this.testPersons.stream().filter(p -> Gender.Male.equals(p.getGender(this.context))).collect(Collectors.toList());
    assertExamplePersonEquals(expectedPersons, oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithMultipleParameters() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE Age >= $Param1 AND LongAge <= $Param2";
    new AddIntegerLongValue(this.context, "Param1", 1L).executeAction();
    new AddIntegerLongValue(this.context, "Param2", 23L).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons.subList(1, 4), oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithoutResettingParameters() throws Exception {
    final String selectSomeWithArgument1 = selectSome + " WHERE Age > $Param";
    new AddIntegerLongValue(this.context, "Param", 2L).executeAction();
    List<IMendixObject> oqlResult1 = new ExecuteOQLStatement(this.context, selectSomeWithArgument1, ExamplePersonResult.entityName, null, null, true)
            .executeAction();

    final String selectSomeWithArgument2 = selectSomeWithArgument1 + " AND LongAge <= $Param2";
    new AddIntegerLongValue(this.context, "Param2", 23L).executeAction();
    List<IMendixObject> oqlResult2 = new ExecuteOQLStatement(this.context, selectSomeWithArgument2, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons.subList(3, TEST_OBJECTS), oqlResult1, someMembers);
    assertExamplePersonEquals(this.testPersons.subList(3, 4), oqlResult2, someMembers);
  }

  @Test
  public void executeOQLStatementWithoutDefiningParameter() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE Age > $Param";
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons, oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithIncorrectParameter() throws Exception {
    new AddIntegerLongValue(this.context, "Param", 2L).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSome, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons, oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithNullParameter() throws Exception {
    final String selectSomeWithArgument = selectSome + " WHERE Age != $Param";
    new AddIntegerLongValue(this.context, "Param", null).executeAction();
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSomeWithArgument, ExamplePersonResult.entityName, null, null, false)
            .executeAction();

    assertExamplePersonEquals(this.testPersons, oqlResult, someMembers);
  }
}
