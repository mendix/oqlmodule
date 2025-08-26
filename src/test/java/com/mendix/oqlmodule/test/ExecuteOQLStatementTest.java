package com.mendix.oqlmodule.test;

import administration.proxies.Account;
import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import oql.actions.ExecuteOQLStatement;
import oql.proxies.ExamplePersonResult;
import oql.proxies.ExamplePersonResult.MemberNames;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
    assertThrows(NullPointerException.class, () ->
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

    assertExamplePersonEquals(this.testPersons, oqlResult, MemberNames.values());
  }
}
