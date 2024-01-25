package umm3601.user;

import java.io.IOException;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

public class TodosController implements Controller {

    private TodosDatabase userDatabase;
     /**
        @param database the `Database` containing user data
     */
    public TodosController(TodosDatabase todosDatabase) {
        this.todosDatabase = todosDatabase;
    }

        @throwsIOException

    public static TodosController buildTodosController(String userDataFile) throws IOException {
        TodosController todosController = null;

        TodosDatabase todosDatabase = new TodosDatabase(todosDataFile);
        todosController = new TodosController(todosDatabase);

        return todosController;
    }
      /**
        @param ctx a Javalin HTTP context
      */
    public void getTodos(Context ctx) {
        String id = ctx.pathParam("id");
        Todos todos = todosDatabase.getTodos(id);
        if (todos != null) {
            ctx.json(todos);
            ctx.status(HttpStatus.OK);
        } else {
            throw new NotFoundResponse("No todos with id " + id + " was found.");
        }
    }

        /**
        @param ctx a Javalin HTTP context
      */
    public void getTodos(Context ctx) {
        Todo[] todos = todosDatabase.listTodos(ctx.queryParamMap());
        ctx.json(todos);
    }
      /**
        @param server The Javalin server instance
      */
    @Override
    public void addRoutes(Javalin server) {
        server.get("/api/todos/{id}", this::getTodos);
        server.get("/api/todos", this::getTodos);
        server.get("/api/todos/58895985c1849992336c219b", this::getTodos);
    }
}
