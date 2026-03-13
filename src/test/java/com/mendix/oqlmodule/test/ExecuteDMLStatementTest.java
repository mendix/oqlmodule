package com.mendix.oqlmodule.test;

import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import oql.actions.ExecuteDMLStatement;
import oqlexample.proxies.ExampleData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExecuteDMLStatementTest extends OQLStatementTestSkeleton {

  @Test
  public void executeValidUpdateDMLStatement() throws Exception {
    ExampleData example = new ExampleData(this.context);
    example.setContents("Before");
    example.commit();
    IMendixIdentifier exampleObjectId = example.getMendixObject().getId();

    String updateStmt = "UPDATE OQLExample.ExampleData SET Contents = 'After' WHERE id = '" + exampleObjectId.toLong() + "'";

    Long affectedRows = new ExecuteDMLStatement(this.context, updateStmt).executeAction();

    assertEquals(1L, affectedRows);

    ExampleData updatedExample = ExampleData.load(this.context, exampleObjectId);
    assertEquals("After", updatedExample.getContents());
  }

  @Test
  public void executeValidDeleteDMLStatement() throws Exception {
    ExampleData example = new ExampleData(this.context);
    example.setContents("ToDelete");
    example.commit();

    String deleteStmt =
      "DELETE FROM OQLExample.ExampleData WHERE Contents = 'ToDelete'";

    Long affectedRows = new ExecuteDMLStatement(this.context, deleteStmt).executeAction();

    assertEquals(1L, affectedRows);

    List<?> deletedObjects = ExampleData.load(this.context, "[Contents = 'ToDelete']");
    assertEquals(0, deletedObjects.size());
  }

  @Test
  public void executeDMLWithNullStatement() {
    assertThrows(MendixRuntimeException.class, () ->
      new ExecuteDMLStatement(this.context, null).executeAction()
    );
  }

  @Test
  public void executeDMLWithInvalidStatement() {
    assertThrows(Exception.class, () ->
      new ExecuteDMLStatement(this.context, "BAD OQL").executeAction()
    );
  }
}

