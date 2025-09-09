package com.mendix.oqlmodule.test;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.*;
import com.mendix.test.run.ProjectRunner;
import oql.implementation.OQL;
import oql.proxies.ExamplePerson;
import oql.proxies.ExamplePersonResult;
import oql.proxies.ExamplePersonResult.MemberNames;
import oql.proxies.Gender;
import org.junit.jupiter.api.*;
import system.proxies.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class OQLStatementTestSkeleton {

  protected static final String selectStar = "SELECT * FROM OQL.ExamplePerson";
  protected static final String selectAll =
    "SELECT id ExamplePersonResult_ExamplePerson, Number, Name, DateOfBirth, Age, LongAge, Active, HeightInDecimal, Gender, OQL.MarriedTo Result_MarriedTo FROM OQL.ExamplePerson";
  protected static final String selectSome = "SELECT id ExamplePersonResult_ExamplePerson, Name, Age FROM OQL.ExamplePerson";
  protected static final MemberNames[] someMembers = new MemberNames[]{MemberNames.ExamplePersonResult_ExamplePerson, MemberNames.Name, MemberNames.Age};
  protected static final int TEST_OBJECTS = 5;

  protected List<ExamplePerson> testPersons;
  protected IContext context;
  protected ILogNode logger;
  protected IUser user;

  public OQLStatementTestSkeleton() {
    ProjectRunner.run();
    this.logger = Core.getLogger(this.getClass().getName());
  }

  @BeforeAll
  public void prepare() throws CoreException {
    IContext prepareContext = Core.createSystemContext();

    // Get the user, if present, or create one, if not:
    this.user = Core.getUser(prepareContext, "TestUser");
    if (this.user == null) {
      User user = new User(prepareContext);
      user.setName("TestUser");
      user.setPassword("Mendix123");
      user.commit();
      this.user = Core.getUser(prepareContext, "TestUser");
    }

    this.testPersons = new ArrayList<>();
    ExamplePerson marriedTo = null;
    for (int i = 0; i < TEST_OBJECTS; i++) {
      ExamplePerson newPerson = new ExamplePerson(prepareContext);
      newPerson.setActive(i % 2 == 0);
      newPerson.setAge(i);
      newPerson.setDateOfBirth(new GregorianCalendar(100, i % 12, i % 30 + 1).getTime());
      newPerson.setGender(Gender.values()[i % 3]);
      newPerson.setHeightInDecimal(new BigDecimal(i + 170));
      newPerson.setLongAge(i + 20L);
      newPerson.setMarriedTo(marriedTo);
      newPerson.setName("Example\r\n\\Person " + i);
      this.testPersons.add(newPerson);
      marriedTo = marriedTo == null ? newPerson : null; // Alternate between null and current
    }
    Core.commit(prepareContext, this.testPersons.stream().map(ExamplePerson::getMendixObject).collect(Collectors.toList()));
  }

  @AfterAll
  public void cleanUp() {
    IContext cleanUpContext = Core.createSystemContext();
    Core.delete(cleanUpContext, Core.createXPathQuery("//OQL.ExamplePerson").execute(cleanUpContext));
  }

  @BeforeEach
  public void setUpContext() throws CoreException {
    ISession session = Core.initializeSession(this.user, null);
    this.context = session.createContext().createSudoClone();
    this.context.startTransaction();
  }

  @AfterEach
  public void cleanUpParameters() {
    OQL.resetParameters();
    this.context.rollbackTransaction();
    Core.logout(this.context.getSession());
  }

  protected void assertExamplePersonEquals(List<ExamplePerson> expected, List<IMendixObject> results, ExamplePersonResult.MemberNames[] membersToCheck) throws CoreException {
    assertEquals(expected.size(), results.size());

    Iterator<ExamplePerson> it = expected.iterator();
    for (IMendixObject resultObject : sortByNumber(results)) {

      ExamplePerson existingPerson = it.next();
      ExamplePersonResult resultPerson = ExamplePersonResult.initialize(this.context, resultObject);

      for (ExamplePersonResult.MemberNames member : membersToCheck) {
        switch (member) {
          case ExamplePersonResult_ExamplePerson:
            assertEqualIds(existingPerson, resultPerson.getExamplePersonResult_ExamplePerson());
            break;
          case Result_MarriedTo:
            assertEqualIds(existingPerson.getMarriedTo(), resultPerson.getResult_MarriedTo());
            break;
          case HeightInDecimal:
            assertEquals(0, existingPerson.getHeightInDecimal().compareTo(resultPerson.getHeightInDecimal()));
            break;
          default:
            Object expectedValue = existingPerson.getMendixObject().getValue(this.context, member.toString());
            Object resultValue = resultPerson.getMendixObject().getValue(this.context, member.toString());
            assertEquals(expectedValue, resultValue);
        }
      }
    }
  }

  private List<IMendixObject> sortByNumber(List<IMendixObject> listToSort) {
    return listToSort.stream().sorted((o1, o2) -> {
      Long n1 = o1.getValue(this.context, ExamplePersonResult.MemberNames.Number.toString());
      Long n2 = o2.getValue(this.context, ExamplePersonResult.MemberNames.Number.toString());
      return (int) (n1 - n2);
    }).collect(Collectors.toList());
  }

  private void assertEqualIds(ExamplePerson expected, ExamplePerson result) {
    IMendixIdentifier expectedId = expected != null ? expected.getMendixObject().getId() : null;
    IMendixIdentifier resultId = result != null ? result.getMendixObject().getId() : null;
    assertEquals(expectedId, resultId);
  }
}
