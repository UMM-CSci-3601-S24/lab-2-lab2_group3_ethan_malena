package umm3601.todos;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
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
        Boolean targetStatus = null;
        if (statusParam.equals("complete")) {
          targetStatus = true;
        } else if (statusParam.equals("incomplete")) {
          targetStatus = false;
        } else {
          throw new BadRequestResponse("Specified status '" + statusParam + "' must be complete or incomplete");
        }
        filteredTodos = filterTodosByStatus(filteredTodos, targetStatus);
    }

    if (queryParams.containsKey("owner")) {
      String targetOwner = queryParams.get("owner").get(0);
      filteredTodos = filterTodosByOwner(filteredTodos, targetOwner);
    }

    if (queryParams.containsKey("orderBy")) {
      String targetOrder = queryParams.get("orderBy").get(0);
      if (targetOrder.equals("status")) {
        Arrays.sort(filteredTodos, Comparator.comparing((todo) -> todo.status));
      } else if (targetOrder.equals("owner")) {
        Arrays.sort(filteredTodos, Comparator.comparing((todo) -> todo.owner));
      } else if (targetOrder.equals("body")) {
        Arrays.sort(filteredTodos, Comparator.comparing((todo) -> todo.body));
      } else if (targetOrder.equals("category")) {
        Arrays.sort(filteredTodos, Comparator.comparing((todo) -> todo.category));
      } else {
        throw new BadRequestResponse("Specified orderBy parameter '" + targetOrder
        + "' must be status, owner, body, or category");
      }
    }

    if (queryParams.containsKey("category")) {
      String targetCategory = queryParams.get("category").get(0);
      filteredTodos = filterTodosByCategory(filteredTodos, targetCategory);
    }


    if (queryParams.containsKey("contains")) {
      String targetContains = queryParams.get("contains").get(0);
      filteredTodos = filterTodosByContains(filteredTodos, targetContains);
    }

    if (queryParams.containsKey("limit")) {
      String limitParam = queryParams.get("limit").get(0);
      try {
        int targetLim = Integer.parseInt(limitParam);
        int targetLimit = Integer.min(targetLim, filteredTodos.length);
        filteredTodos = Arrays.copyOfRange(filteredTodos, 0, targetLimit);
        } catch (NumberFormatException e) {
          throw new BadRequestResponse("Specified limit '" + limitParam + "' can't be parsed to an integer");
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

  public Todos[] filterTodosByCategory(Todos[] todos, String targetCategory) {
    return Arrays.stream(todos).filter(x -> x.category.equals(targetCategory)).toArray(Todos[]::new);
  }

  public Todos[] filterTodosByContains(Todos[] todos, String targetContains) {
    return Arrays.stream(todos).filter(x -> x.body.contains(targetContains)).toArray(Todos[]::new);
  }

}
