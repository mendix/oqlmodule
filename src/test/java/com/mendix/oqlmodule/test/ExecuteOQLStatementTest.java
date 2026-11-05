package com.mendix.oqlmodule.test;

import administration.proxies.Account;
import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import oql.actions.ExecuteOQLStatement;
import oql.proxies.ExamplePersonResult;
import oql.proxies.ExamplePersonResult.MemberNames;
import oqlexample.proxies.ExampleAssocTarget;
import oqlexample.proxies.ExampleData;
import oqlexample.proxies.ExampleDataSpecialization;
import oqlexample.proxies.ExampleResultNPE;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExecuteOQLStatementTest extends OQLStatementTestSkeleton {

  @Test
  public void executeOQLStatementWithAllAttributes() throws Exception {
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectAll, ExamplePersonResult.entityName, null, null, false)
      .executeAction();

    assertExamplePersonEquals(this.testPersons, oqlResult, MemberNames.values());
  }

  @Test
  public void executeOQLStatementWithOffsetAndAmount() throws Exception {
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectAll, ExamplePersonResult.entityName, 2L, 2L, false)
      .executeAction();

    assertExamplePersonEquals(this.testPersons.subList(2, 4), oqlResult, MemberNames.values());
  }

  @Test
  public void executeOQLStatementWithSomeAttributes() throws Exception {
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectSome, ExamplePersonResult.entityName, null, null, false)
      .executeAction();

    assertExamplePersonEquals(this.testPersons, oqlResult, someMembers);
  }

  @Test
  public void executeOQLStatementWithNullQuery() {
    assertThrows(MendixRuntimeException.class, () ->
      new ExecuteOQLStatement(this.context, null, ExamplePersonResult.entityName, null, null, false).executeAction()
    );
  }

  @Test
  public void executeOQLStatementWithInvalidReturnEntityName() {
    assertThrows(MendixRuntimeException.class, () ->
      new ExecuteOQLStatement(this.context, selectAll, "AnyNPEWillDo", null, null, false).executeAction()
    );
  }

  @Test
  public void executeOQLStatementWithIncompatibleReturnEntityName() {
    assertThrows(RuntimeException.class, () ->
      new ExecuteOQLStatement(this.context, selectAll, Account.entityName, null, null, false).executeAction()
    );
  }

  @Test
  public void executeOQLStatementFromDataset() throws Exception {
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, "OQL.DataSet", ExamplePersonResult.entityName, null, null, false)
      .executeAction();

    MemberNames[] members = Arrays.stream(MemberNames.values()).filter(member -> !MemberNames.Result_MarriedTo.equals(member)).toArray(MemberNames[]::new);
    assertExamplePersonEquals(this.testPersons, oqlResult, members);
  }

  @Test
  public void executeOQLStatementWithSelectStar() throws Exception {
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, selectStar, ExamplePersonResult.entityName, null, null, false)
      .executeAction();

    MemberNames[] members = Arrays.stream(MemberNames.values())
      .filter(member -> !MemberNames.ExamplePersonResult_ExamplePerson.equals(member) && !MemberNames.Result_MarriedTo.equals(member)).toArray(MemberNames[]::new);
    assertExamplePersonEquals(this.testPersons, oqlResult, members);
  }

  @Test
  public void executeOQLStatementWithSelectStarToGeneralization() throws Exception {
    // Create a simple Example
    ExampleData exampleData = new ExampleData(this.context);
    exampleData.setContents("abc");
    exampleData.commit();

    // Perform test
    executeOQLStatementWithExampleData(exampleData.getMendixObject(), "SELECT * FROM OQLExample.ExampleData");
  }

  @Test
  public void executeOQLStatementWithSelectStarToSpecialization() throws Exception {
    // Create a simple Example
    ExampleDataSpecialization exampleData = new ExampleDataSpecialization(this.context);
    exampleData.setContents("abc");
    exampleData.commit();

    // Perform test
    executeOQLStatementWithExampleData(exampleData.getMendixObject(), "SELECT * FROM OQLExample.ExampleDataSpecialization");
  }

  @Test
  public void executeOQLStatementWithInheritedSingleAssociationSet() throws Exception {
    ExampleAssocTarget target = createTarget("target");
    createExampleDataSpecialization(target, null);

    List<IMendixObject> oqlResult = executeInheritedQuery(selectInheritedSingle);

    assertEquals(1, oqlResult.size());
    ExampleResultNPE result = ExampleResultNPE.initialize(this.context, oqlResult.get(0));
    assertEquals(target.getMendixObject().getId(), result.getExampleResultNPE_ExampleAssocTarget().getMendixObject().getId());
  }

  @Test
  public void executeOQLStatementWithInheritedSingleAssociationNull() throws Exception {
    createExampleDataSpecialization(null, null);

    List<IMendixObject> oqlResult = executeInheritedQuery(selectInheritedSingle);

    assertEquals(1, oqlResult.size());
    assertNull(ExampleResultNPE.initialize(this.context, oqlResult.get(0)).getExampleResultNPE_ExampleAssocTarget());
  }

  @Test
  public void executeOQLStatementWithInheritedManyAssociationSet() throws Exception {
    ExampleAssocTarget target1 = createTarget("target1");
    ExampleAssocTarget target2 = createTarget("target2");
    createExampleDataSpecialization(null, Arrays.asList(target1, target2));

    List<IMendixObject> oqlResult = executeInheritedQuery(selectInheritedMany);

    assertEquals(2, oqlResult.size());
    List<ExampleAssocTarget> targets = new ArrayList<>();
    for (IMendixObject object : oqlResult) {
      targets.addAll(ExampleResultNPE.initialize(this.context, object).getExampleResultNPE_ExampleAssocTarget_Many());
    }
    assertEquals(2, targets.size());
  }

  @Test
  public void executeOQLStatementWithInheritedManyAssociationEmpty() throws Exception {
    createExampleDataSpecialization(null, null);

    List<IMendixObject> oqlResult = executeInheritedQuery(selectInheritedMany);

    assertEquals(1, oqlResult.size());
    assertTrue(ExampleResultNPE.initialize(this.context, oqlResult.get(0)).getExampleResultNPE_ExampleAssocTarget_Many().isEmpty());
  }

  @Test
  public void executeOQLStatementJoinThroughInheritedAssociation() throws Exception {
    ExampleAssocTarget target = createTarget("target");
    createExampleDataSpecialization(target, null);

    List<IMendixObject> oqlResult = executeInheritedQuery(
      "SELECT AssocTarget.id ExampleResultNPE_ExampleAssocTarget " +
      "FROM OQLExample.ExampleDataSpecialization Data " +
      "JOIN Data/OQLExample.ExampleData_ExampleAssocTarget/OQLExample.ExampleAssocTarget AssocTarget");

    assertEquals(1, oqlResult.size());
    assertEquals(target.getMendixObject().getId(), ExampleResultNPE.initialize(this.context, oqlResult.get(0)).getExampleResultNPE_ExampleAssocTarget().getMendixObject().getId());
  }

  private void executeOQLStatementWithExampleData(IMendixObject expectedData, String query) throws Exception {
    // Perform some sanity check
    assertNotNull(expectedData.getOwner(this.context));
    assertNotNull(expectedData.getChangedBy(this.context));

    // Execute oqlResult
    List<IMendixObject> oqlResult = new ExecuteOQLStatement(this.context, query, ExampleResultNPE.entityName, null, null, false).executeAction();

    // Validate results
    assertEquals(1, oqlResult.size());
    ExampleResultNPE resultNPE = ExampleResultNPE.initialize(this.context, oqlResult.get(0));

    assertEquals(expectedData.getValue(this.context, "Contents"), resultNPE.getContents());
    assertEquals(expectedData.getValue(this.context, "Countert"), resultNPE.getCountert());
    assertEquals(expectedData.getOwner(this.context), resultNPE.getMendixObject().getOwner(this.context));
    assertEquals(expectedData.getCreatedDate(this.context), resultNPE.getMendixObject().getCreatedDate(this.context));
    assertEquals(expectedData.getChangedBy(this.context), resultNPE.getMendixObject().getChangedBy(this.context));
    assertEquals(expectedData.getChangedDate(this.context), resultNPE.getMendixObject().getChangedDate(this.context));
  }
}
