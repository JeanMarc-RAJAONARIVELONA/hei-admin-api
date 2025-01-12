package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.COURSE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = StudentIT.ContextInitializer.class)
@AutoConfigureMockMvc
class StudentIT {

  @MockBean private SentryConf sentryConf;

  @MockBean private CognitoComponent cognitoComponentMock;

  @MockBean private EventBridgeClient eventBridgeClientMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static Student someUpdatableStudent() {
    return student1()
        .address("Adr 999")
        .sex(Sex.F)
        .lastName("Other last")
        .firstName("Other first")
        .birthDate(LocalDate.parse("2000-01-03"));
  }

  public static Student someCreatableStudent() {
    Student student = student1();
    Faker faker = new Faker();
    student.setId(null);
    student.setFirstName(faker.name().firstName());
    student.setLastName(faker.name().lastName());
    student.setEmail("test+" + randomUUID() + "@hei.school");
    student.setRef("STD21" + (int) (Math.random() * 1_000_000));
    student.setPhone("03" + (int) (Math.random() * 1_000_000_000));
    student.setStatus(EnableStatus.ENABLED);
    student.setSex(Math.random() < 0.3 ? Sex.F : Sex.M);
    Instant birthday = Instant.parse("1993-11-30T18:35:24.00Z");
    int ageOfEntrance = 14 + (int) (Math.random() * 20);
    student.setBirthDate(birthday.atZone(ZoneId.systemDefault()).toLocalDate());
    student.setEntranceDatetime(birthday.plusSeconds(ageOfEntrance * 365L * 24L * 60L * 60L));
    student.setAddress(faker.address().fullAddress());
    return student;
  }

  static List<Student> someCreatableStudentList(int nbOfStudent) {
    List<Student> studentList = new ArrayList<>();
    for (int i = 0; i < nbOfStudent; i++) {
      studentList.add(someCreatableStudent());
    }
    return studentList;
  }

  public static Student student1() {
    Student student = new Student();
    student.setId("student1_id");
    student.setFirstName("Ryan");
    student.setLastName("Andria");
    student.setEmail("test+ryan@hei.school");
    student.setRef("STD21001");
    student.setPhone("0322411123");
    student.setStatus(EnableStatus.ENABLED);
    student.setSex(Sex.M);
    student.setBirthDate(LocalDate.parse("2000-01-01"));
    student.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    student.setAddress("Adr 1");
    return student;
  }

  public static Student student2() {
    Student student = new Student();
    student.setId("student2_id");
    student.setFirstName("Two");
    student.setLastName("Student");
    student.setEmail("test+student2@hei.school");
    student.setRef("STD21002");
    student.setPhone("0322411124");
    student.setStatus(EnableStatus.ENABLED);
    student.setSex(Sex.F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    return student;
  }

  public static Student student3() {
    Student student = new Student();
    student.setId("student3_id");
    student.setFirstName("Three");
    student.setLastName("Student");
    student.setEmail("test+student3@hei.school");
    student.setRef("STD21003");
    student.setPhone("0322411124");
    student.setStatus(EnableStatus.ENABLED);
    student.setSex(Sex.F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    return student;
  }

  public static Student disabledStudent1() {
    return new Student()
        .id("student4_id")
        .firstName("Disable")
        .lastName("One")
        .email("test+disable1@hei.school")
        .ref("STD29001")
        .status(EnableStatus.DISABLED)
        .sex(Sex.M)
        .birthDate(LocalDate.parse("2000-12-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .phone("0322411123")
        .address("Adr 1");
  }

  public static Student creatableSuspendedStudent() {
    return new Student()
        .firstName("Suspended")
        .lastName("Two")
        .email("test+suspended2@hei.school")
        .ref("STD29004")
        .status(EnableStatus.SUSPENDED)
        .sex(Sex.F)
        .birthDate(LocalDate.parse("2000-12-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .phone("0322411124")
        .address("Adr 3");
  }

  public static Student suspendedStudent1() {
    return new Student()
        .id("student6_id")
        .firstName("Suspended")
        .lastName("One")
        .email("test+suspended@hei.school")
        .ref("STD29003")
        .status(EnableStatus.SUSPENDED)
        .sex(Sex.F)
        .birthDate(LocalDate.parse("2000-12-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .phone("0322411124")
        .address("Adr 2");
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  @DirtiesContext
  void student_update_own_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(student1Client);
    UsersApi managerApi = new UsersApi(manager1Client);
    api.getStudentById(STUDENT1_ID);
    Student actual = api.updateStudent(STUDENT1_ID, someUpdatableStudent());
    List<Student> actualStudents =
        managerApi.getStudents(1, 10, null, null, null, null, null, null);

    assertTrue(actualStudents.contains(actual));
  }

  @Test
  void student_read_own_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    Student actual = api.getStudentById(STUDENT1_ID);

    assertEquals(student1(), actual);
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    assertThrowsForbiddenException(() -> api.getStudentById(TestUtils.STUDENT2_ID));

    assertThrowsForbiddenException(
        () -> api.getStudents(1, 20, null, null, null, null, null, null));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(teacher1Client);
    Student actualStudent1 = api.getStudentById(STUDENT1_ID);

    List<Student> actualStudents = api.getStudents(1, 20, null, null, null, null, null, null);

    assertEquals(student1(), actualStudent1);
    assertTrue(actualStudents.contains(student1()));
    assertTrue(actualStudents.contains(student2()));

    List<Student> actualStudents2 =
        api.getStudents(1, 10, null, null, null, COURSE2_ID, null, null);

    assertEquals(student1(), actualStudents2.get(0));
    assertEquals(2, actualStudents2.size());
  }

  @Test
  void manager_read_by_disabled_status_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 10, null, null, null, null, EnableStatus.DISABLED, null);
    assertEquals(2, actualStudents.size());
    assertTrue(actualStudents.contains(disabledStudent1()));
  }

  @Test
  void manager_read_by_suspended_status_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 10, null, null, null, null, EnableStatus.SUSPENDED, null);
    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(suspendedStudent1()));
  }

  @Test
  void manager_read_by_status_and_sex_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 10, null, null, null, null, EnableStatus.DISABLED, Sex.F);
    assertEquals(1, actualStudents.size());
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    assertThrowsForbiddenException(() -> api.createOrUpdateStudents(List.of()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(teacher1Client);

    assertThrowsForbiddenException(() -> api.createOrUpdateStudents(List.of()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents = api.getStudents(1, 20, null, null, null, null, null, null);
    List<Student> actualStudents2 =
        api.getStudents(1, 10, null, null, null, COURSE2_ID, null, null);

    assertTrue(actualStudents.contains(student1()));
    assertTrue(actualStudents.contains(student2()));

    assertEquals(student1(), actualStudents2.get(0));
    assertEquals(2, actualStudents2.size());
  }

  @Test
  void manager_read_by_ref_and_name_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(
            1,
            20,
            student1().getRef(),
            student1().getFirstName(),
            student1().getLastName(),
            null,
            null,
            null);

    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student1()));
  }

  @Test
  void manager_read_by_ref_ignoring_case_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents = api.getStudents(1, 20, "std21001", null, null, null, null, null);

    assertEquals("STD21001", student1().getRef());
    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student1()));
  }

  @Test
  void manager_read_by_ref_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 20, student1().getRef(), null, null, null, null, null);

    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student1()));
  }

  @Test
  void manager_read_by_last_name_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 20, null, null, student2().getLastName(), null, null, null);

    assertEquals(2, actualStudents.size());
    assertTrue(actualStudents.contains(student2()));
    assertTrue(actualStudents.contains(student3()));
  }

  @Test
  void manager_read_by_ref_and_last_name_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(
            1, 20, student2().getRef(), null, student2().getLastName(), null, null, null);

    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student2()));
  }

  @Test
  void manager_read_by_ref_and_bad_name_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(
            1, 20, student2().getRef(), null, student1().getLastName(), null, null, null);

    assertEquals(0, actualStudents.size());
    assertFalse(actualStudents.contains(student1()));
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    List<Student> toUpdate =
        api.createOrUpdateStudents(List.of(someCreatableStudent(), someCreatableStudent()));
    Student toUpdate0 = toUpdate.get(0);
    toUpdate0.setLastName("A new name zero");
    Student toUpdate1 = toUpdate.get(1);
    toUpdate1.setLastName("A new name one");

    List<Student> updated = api.createOrUpdateStudents(toUpdate);

    assertEquals(2, updated.size());
    assertTrue(updated.contains(toUpdate0));
    assertTrue(updated.contains(toUpdate1));
  }

  @Test
  void manager_write_update_rollback_on_event_error() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    Student toCreate = someCreatableStudent();
    reset(eventBridgeClientMock);
    when(eventBridgeClientMock.putEvents((PutEventsRequest) any()))
        .thenThrow(RuntimeException.class);

    assertThrowsApiException(
        "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":null}",
        () -> api.createOrUpdateStudents(List.of(toCreate)));

    List<Student> actual = api.getStudents(1, 100, null, null, null, null, null, null);
    assertFalse(actual.stream().anyMatch(s -> Objects.equals(toCreate.getEmail(), s.getEmail())));
  }

  @Test
  void manager_write_update_more_than_10_students_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    Student studentToCreate = someCreatableStudent();
    List<Student> listToCreate = someCreatableStudentList(11);
    listToCreate.add(studentToCreate);

    assertThrowsApiException(
        "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":\"Request entries must be <= 10\"}",
        () -> api.createOrUpdateStudents(listToCreate));

    List<Student> actual = api.getStudents(1, 100, null, null, null, null, null, null);
    assertFalse(
        actual.stream().anyMatch(s -> Objects.equals(studentToCreate.getEmail(), s.getEmail())));
  }

  @Test
  void manager_write_update_triggers_userUpserted() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    reset(eventBridgeClientMock);
    when(eventBridgeClientMock.putEvents((PutEventsRequest) any()))
        .thenReturn(
            PutEventsResponse.builder()
                .entries(
                    PutEventsResultEntry.builder().eventId("eventId1").build(),
                    PutEventsResultEntry.builder().eventId("eventId2").build())
                .build());

    List<Student> created =
        api.createOrUpdateStudents(List.of(someCreatableStudent(), someCreatableStudent()));

    ArgumentCaptor<PutEventsRequest> captor = ArgumentCaptor.forClass(PutEventsRequest.class);
    verify(eventBridgeClientMock, times(1)).putEvents(captor.capture());
    PutEventsRequest actualRequest = captor.getValue();
    List<PutEventsRequestEntry> actualRequestEntries = actualRequest.entries();
    assertEquals(2, actualRequestEntries.size());
    Student created0 = created.get(0);
    PutEventsRequestEntry requestEntry0 = actualRequestEntries.get(0);
    assertTrue(requestEntry0.detail().contains(created0.getId()));
    assertTrue(requestEntry0.detail().contains(created0.getEmail()));
    Student created1 = created.get(1);
    PutEventsRequestEntry requestEntry1 = actualRequestEntries.get(1);
    assertTrue(requestEntry1.detail().contains(created1.getId()));
    assertTrue(requestEntry1.detail().contains(created1.getEmail()));
  }

  @Test
  @DirtiesContext
  void manager_write_suspended_student() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actual = api.createOrUpdateStudents(List.of(creatableSuspendedStudent()));
    Student created = actual.get(0);
    List<Student> suspended =
        api.getStudents(1, 10, null, "Suspended", null, null, EnableStatus.SUSPENDED, null);

    assertTrue(suspended.contains(created));
    assertEquals(1, actual.size());
  }

  @Test
  @DirtiesContext
  void manager_update_student_to_suspended() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actual =
        api.createOrUpdateStudents(List.of(student2().status(EnableStatus.SUSPENDED)));
    Student updated = actual.get(0);
    List<Student> suspended =
        api.getStudents(1, 10, null, null, null, null, EnableStatus.SUSPENDED, null);

    assertTrue(suspended.contains(updated));
    assertEquals(1, actual.size());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
