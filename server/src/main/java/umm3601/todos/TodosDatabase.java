package umm3601.todos;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.http.BadRequestResponse;

//import io.javalin.http.BadRequestResponse;

public class TodosDatabase {

  private Todos[] allTodos;

  public TodosDatabase(String todoDataFile) throws IOException {
    InputStream resourceAsStream = getClass().getResourceAsStream(todoDataFile);
    if (resourceAsStream == null) {
      throw new IOException("Could not find " + todoDataFile);
    }
    InputStreamReader reader = new InputStreamReader(resourceAsStream);
    ObjectMapper objectMapper = new ObjectMapper();
    allTodos = objectMapper.readValue(reader, Todos[].class);
  }

  public int size() {
    return allTodos.length;
  }

  /**
   * @param id the ID of the desired todo
   * @return the todo with the given ID, or null if there is no user with that ID
   */
  public Todos getTodo(String id) {
    return Arrays.stream(allTodos).filter(x -> x._id.equals(id)).findFirst().orElse(null);
  }

  /**
   * @param queryParams map of key-value pairs for the query
   * @return an array of all the user matching the given criteria
   */
  public Todos[] listTodos(Map<String, List<String>> queryParams) {
    Todos[] filteredTodos = allTodos;

    if (queryParams.containsKey("status")) {
      String statusParam = queryParams.get("status").get(0);
      try {
        Boolean targetStatus = null;
        if (statusParam == "complete") {
          targetStatus = true;
        }
        if (statusParam == "incomplete") {
          targetStatus = false;
        }
        filteredTodos = filterTodosByStatus(filteredTodos, targetStatus);
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Specified status '" + statusParam + "' is not complete or incomplete");
      }
    }

    if (queryParams.containsKey("owner")) {
      String targetOwner = queryParams.get("owner").get(0);
      filteredTodos = filterTodosByOwner(filteredTodos, targetOwner);
    }

    if (queryParams.containsKey("orderBy")) {
      String orderParam = queryParams.get("orderBy").get(0);
      Arrays.sort(filteredTodos);
      try {
        String targetOrder = null;
        if (orderParam == "category") {
          targetOrder = "category";
        }
        if (orderParam == "owner") {
          targetOrder = "owner";
        }
        if (orderParam == "body") {
          targetOrder = "body";
        }
        if (orderParam == "status") {
          targetOrder = "status";
        }
        filteredTodos = Arrays.sort(filteredTodos, queryParams.get(targetOrder));
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Specified status '" + statusParam + "' is not complete or incomplete");
      }
    }

    return filteredTodos;
  }

  /**
   * @param todos       the list of users to filer by owner
   * @param targetOwner the target owner to look for
   * @return an array of all the users from the given list that have the target
   *         owner
   */
  public Todos[] filterTodosByOwner(Todos[] todos, String targetOwner) {
    return Arrays.stream(todos).filter(x -> x.owner.equals(targetOwner)).toArray(Todos[]::new);
  }

  public Todos[] filterTodosByStatus(Todos[] todos, Boolean targetStatus) {
    return Arrays.stream(todos).filter(x -> x.status.equals(targetStatus)).toArray(Todos[]::new);
  }

  public Todos[] filterTodosByOrder(Todos[] todos, String targetOrder) {
    return Arrays.stream(todos).filter(x -> x.orderBy.equals(targetOrder)).toArray(Todos[]::new);
  }

}
