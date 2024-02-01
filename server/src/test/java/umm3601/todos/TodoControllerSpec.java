package umm3601.todos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
import static org.mockito.Mockito.when;

import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.javalin.Javalin;
//import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
//import io.javalin.http.HttpStatus;
//import io.javalin.http.NotFoundResponse;
import umm3601.Main;

/**
 * @throws IOException
 */

@SuppressWarnings({ "MagicNumber" })
public class TodoControllerSpec {
  private TodosController todosController;
  private static TodosDatabase db;
  @Mock
  private Context ctx;

  @Captor
  private ArgumentCaptor<Todos[]> todoArrayCaptor;

  /**
   * @throws IOException
   */

  @BeforeEach
  public void setup() throws IOException {
    MockitoAnnotations.openMocks(this);
    db = new TodosDatabase(Main.TODOS_DATA_FILE);
    todosController = new TodosController(db);
  }

  @Test
  public void canBuildController() throws IOException {
    TodosController controller = TodosController.buildTodosController(Main.TODOS_DATA_FILE);
    Javalin mockServer = Mockito.mock(Javalin.class);
    controller.addRoutes(mockServer);
    verify(mockServer, Mockito.atLeast(2)).get(any(), any());
  }

  @Test
  public void buildControllerFailsWithIllegalDbFile() {
    Assertions.assertThrows(IOException.class, () -> {
      TodosController.buildTodosController("this is not a legal file name");
    });
  }

  /**
   * @throws IOException if there are any problems reading from the database file.
   */
  @Test
  public void canGetAllTodos() throws IOException {
    todosController.getTodos(ctx);
    verify(ctx).json(todoArrayCaptor.capture());
    assertEquals(db.size(), todoArrayCaptor.getValue().length);
  }

  /**
   * @throws IOException if there are any problems reading the db file.
   */
  @Test
  public void respondsAppropriatelyToRequestForNonexistentId() throws IOException {
    when(ctx.pathParam("id")).thenReturn(null);
    Throwable exception = Assertions.assertThrows(NotFoundResponse.class, () -> {
      todosController.getTodo(ctx);
    });
    assertEquals("No todos with id " + null + " was found.", exception.getMessage());
  }

  @Test
  public void canGetTodoWithSpecifiedId() throws IOException {
    String id = "58895985c1849992336c219b";
    Todos todo = db.getTodo(id);
    when(ctx.pathParam("id")).thenReturn(id);
    todosController.getTodo(ctx);

    verify(ctx).json(todo);
    verify(ctx).status(HttpStatus.OK);
  }

  @Test
  public void canGetUsersWithStatusCompete() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    todosController.getTodos(ctx);
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todos todos : todoArrayCaptor.getValue()) {
      assertEquals(true, todos.status);
    }
    assertEquals(143, todoArrayCaptor.getValue().length);
  }

  @Test
  public void canGetTodosWithOwner() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Fry"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    todosController.getTodos(ctx);
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todos todos : todoArrayCaptor.getValue()) {
      assertEquals("Fry", todos.owner);
    }
    assertEquals(61, todoArrayCaptor.getValue().length);
  }

  @Test
  public void canGetTodosWithCategory() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("category", Arrays.asList(new String[] {"groceries"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    todosController.getTodos(ctx);
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todos todos : todoArrayCaptor.getValue()) {
      assertEquals("groceries", todos.category);
    }
    assertEquals(76, todoArrayCaptor.getValue().length);
  }

  @Test
  public void canGetTodosWithGivenStatusAndOwner() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Fry"}));
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todosController.getTodos(ctx);
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todos todos : todoArrayCaptor.getValue()) {
      assertEquals(true, todos.status);
      assertEquals("Fry", todos.owner);
    }
    assertEquals(27, todoArrayCaptor.getValue().length);
  }

  @Test
  public void canGetTodosWithGivenStatusOrderByAndOwner() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Fry"}));
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    queryParams.put("orderBy", Arrays.asList(new String[] {"category"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todosController.getTodos(ctx);
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todos todos : todoArrayCaptor.getValue()) {
      assertEquals(true, todos.status);
      assertEquals("Fry", todos.owner);
    }

    Todos[] todos = todoArrayCaptor.getValue();
    for (int i = 0; i < todos.length - 1; i++) {
    assertTrue(todos[i].category.compareTo(todos[i + 1].category) <= 0);
    }
  }
  @Test
  public void canGetTodosWithGivenLimitStatusAndOwner() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Fry"}));
    queryParams.put("status", Arrays.asList(new String[] {"incomplete"}));
    queryParams.put("limit", Arrays.asList(new String[] {"7"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todosController.getTodos(ctx);
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todos todos : todoArrayCaptor.getValue()) {
      assertEquals(false, todos.status);
      assertEquals("Fry", todos.owner);
    }
    assertEquals(7, todoArrayCaptor.getValue().length);
  }

  @Test
  public void canGetTodosWithLimit() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Fry"}));
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    queryParams.put("limit", Arrays.asList(new String[] {"100"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todosController.getTodos(ctx);
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todos todos : todoArrayCaptor.getValue()) {
      assertEquals(true, todos.status);
      assertEquals("Fry", todos.owner);
    }
    assertEquals(27, todoArrayCaptor.getValue().length);
  }

  @Test
  public void canGetTodosWithContains() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Blanche"}));
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    queryParams.put("limit", Arrays.asList(new String[] {"100"}));
    queryParams.put("contains", Arrays.asList(new String[] {"sunt"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todosController.getTodos(ctx);
    verify(ctx).json(todoArrayCaptor.capture());
    for (Todos todos : todoArrayCaptor.getValue()) {
      assertEquals(true, todos.status);
      assertEquals("Blanche", todos.owner);
      assertTrue(todos.body.contains("sunt"));
    }
    assertEquals(6, todoArrayCaptor.getValue().length);
  }
}
