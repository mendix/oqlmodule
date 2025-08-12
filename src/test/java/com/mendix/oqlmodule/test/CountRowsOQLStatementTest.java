package com.mendix.oqlmodule.test;

import com.mendix.systemwideinterfaces.MendixRuntimeException;
import oql.actions.CountRowsOQLStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CountRowsOQLStatementTest extends OQLStatementTestSkeleton {

  @Test
  public void countRowsOQLStatement() throws Exception {
    Long count = new CountRowsOQLStatement(this.context, selectAll, 1000L, 1L).executeAction();

    assertEquals(5L, count);
  }

  @Test
  public void countRowsOQLStatementWithSmallOffsetAndAmount() throws Exception {
    Long count = new CountRowsOQLStatement(this.context, selectAll, 2L, 1L).executeAction();

    assertEquals(2L, count);
  }

  @Test
  public void countRowsOQLStatementWithNullAmount() throws Exception {
    assertThrows(NullPointerException.class, () -> new CountRowsOQLStatement(this.context, selectAll, null, null).executeAction());
  }

  @Test
  public void countRowsOQLStatementWithNullQuery() {
    assertThrows(MendixRuntimeException.class, () -> new CountRowsOQLStatement(this.context, null, 1000L, null).executeAction());
  }

  @Test
  public void countRowsOQLStatementFromDataset() throws Exception {
    assertThrows(MendixRuntimeException.class, () -> new CountRowsOQLStatement(this.context, "OQL.DataSet", 1000L, null).executeAction());
  }

  @Test
  public void countRowsOQLStatementWithSelectStar() throws Exception {
    assertThrows(NullPointerException.class, () -> new CountRowsOQLStatement(this.context, selectStar, null, null).executeAction());
  }
}
