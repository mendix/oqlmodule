package com.mendix.oqlmodule.test;

import com.mendix.systemwideinterfaces.MendixRuntimeException;
import oql.actions.CountRowsOQLStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CountRowsOQLStatementTest extends OQLStatementTestSkeleton {

  @Test
  public void countRowsOQLStatement() throws Exception {
    Long count = new CountRowsOQLStatement(this.context, selectAll, 0L, null).executeAction();

    assertEquals(5, count);
  }

  @Test
  public void countRowsOQLStatementWithOffset() throws Exception {
    Long count = new CountRowsOQLStatement(this.context, selectAll, 0L, 1L).executeAction();

    assertEquals(5, count);
  }

  @Test
  public void countRowsOQLStatementWithOffsetAndNonZeroAmount() throws Exception {
    Long count = new CountRowsOQLStatement(this.context, selectAll, 2L, 1L).executeAction();

    assertEquals(2L, count);
  }

  @Test
  public void countRowsOQLStatementWithNonZeroAmount() throws Exception {
    Long count = new CountRowsOQLStatement(this.context, selectAll, 2L, null).executeAction();

    assertEquals(2, count);
  }

  @Test
  public void countRowsOQLStatementWithNullAmount() {
    assertThrows(NullPointerException.class, () -> new CountRowsOQLStatement(this.context, selectAll, null, null).executeAction());
  }

  @Test
  public void countRowsOQLStatementWithNullQuery() {
    assertThrows(MendixRuntimeException.class, () -> new CountRowsOQLStatement(this.context, null, 1000L, null).executeAction());
  }

  @Test
  public void countRowsOQLStatementFromDataset() {
    assertThrows(MendixRuntimeException.class, () -> new CountRowsOQLStatement(this.context, "OQL.DataSet", 1000L, null).executeAction());
  }

  @Test
  public void countRowsOQLStatementWithSelectStar() {
    assertThrows(NullPointerException.class, () -> new CountRowsOQLStatement(this.context, selectStar, null, null).executeAction());
  }
}
