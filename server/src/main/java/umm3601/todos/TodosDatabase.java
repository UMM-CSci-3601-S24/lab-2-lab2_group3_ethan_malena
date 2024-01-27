package umm3601.todos;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.http.BadRequestResponse;

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

    if (queryParams.containsKey("age")) {
      String ageParam = queryParams.get("age").get(0);
      try {
        int targetAge = Integer.parseInt(ageParam);
        filteredTodos = filterTodosByAge(filteredTodos, targetAge);
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Specified age " + ageParam + "' can't be parsed to an integer");
      }
    }
    if (queryParams.containsKey("company")) {
      String targetCompany = queryParams.get("company").get(0);
      filteredTodos = filterTodosByCompany(filteredTodos, targetCompany);
    }

    return filteredTodos;
  }

  /**
   * @param todos     the list of users to filter by age
   * @param targetAge the target age to look for
   * @return an array of all the users from the given list that have the target
   *         age
   */
  public Todos[] filterTodosByAge(Todos[] todos, int targetAge) {
    return Arrays.stream(todos).filter(x -> x.age == targetAge).toArray(Todos[]::new);
  }

  /**
   * @param todos         the list of users to filer by company
   * @param targetCompany the target company to look for
   * @return an array of all the users from the given list that have the target
   *         company
   */
  public Todos[] filterTodosByCompany(Todos[] todos, String targetCompany) {
    return Arrays.stream(todos).filter(x -> x.company.equals(targetCompany)).toArray(Todos[]::new);
  }

}
