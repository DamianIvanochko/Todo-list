import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;

public class TodoList {
    public static void main(String[] args) {
        int countCatchClause = 0;

        while(true) {
            Scanner sc = new Scanner(System.in);
            String url = "jdbc:postgresql://localhost:5432/postgres";

            try {
                Connection con = DriverManager.getConnection(url, "postgres", "damian22");
                Statement stmt = con.createStatement();

                System.out.println("Write what you want to do:");
                System.out.println("1 - add new task");
                System.out.println("2 - edit task(mark as complete, change finish time, change username, change name of the task)");
                System.out.println("3 - delete task");
                System.out.println("4 - view your tasks");
                System.out.println("5 - stop program");
                System.out.print("Your input: ");

                int inputNum = sc.nextInt();

                if(inputNum == 1) addNewTask(con);
                else if(inputNum == 2) editTask(con);
                else if(inputNum == 3) deleteTask(con);
                else if(inputNum == 4) viewTasksOfUser(con);
                else if(inputNum == 5) break;

                con.close();
                stmt.close();
            }
            catch (SQLException | InputMismatchException e) {
                countCatchClause++;
                if(countCatchClause == 1) System.out.println("Just write a number 1-5 ");
                else if(countCatchClause == 2) System.out.println("Bro write a number between 1 and 5");
                else if(countCatchClause == 3) System.out.println("ти дебіл?");
                else if(countCatchClause == 4) System.out.println("Do you know what a number is?");
                else if(countCatchClause == 5) System.out.println("Kurwa napisz numer kretyna!");
                else if(countCatchClause == 6) System.out.println("...");
                else throw new RuntimeException("You should stop using this program");

                System.out.println();
            }
        }
    }

    private static void addNewTask(Connection con) {
        LocalDate currentDate = LocalDate.now();
        Scanner sc = new Scanner(System.in);

        System.out.println("Write your username:");
        String username = sc.nextLine();

        System.out.println("Write a task to add:");
        String taskToAdd = changeWordCase(sc.nextLine());

        System.out.println("Write a number of days until deadline:");
        int numberOfDaysToAdd = 0;

        try {
            numberOfDaysToAdd = sc.nextInt();
        }
        catch (InputMismatchException e) {
            throw new RuntimeException("Шо хоч се до сметрі робити?");
        }

        if(numberOfDaysToAdd < 0) throw new RuntimeException("Ти дебіл, як ти хочеш поставити від'ємне число днів?");

        LocalDate futureDate = currentDate.plusDays(numberOfDaysToAdd);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedFutureDate = futureDate.format(formatter);

        try {
            PreparedStatement addNewTask = con.prepareStatement("INSERT INTO todo_list(Username, Task, FinishTime, Status)" +
                    "VALUES(?, ?, ?, ?)");
            addNewTask.setString(1, username);
            addNewTask.setString(2, taskToAdd);
            addNewTask.setString(3, formattedFutureDate);
            addNewTask.setString(4, "❌");

            int rowsAffected = addNewTask.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Task added successfully. \uD83C\uDFAF");
            } else {
                System.out.println("No rows added. ❌");
            }

            System.out.println();

            addNewTask.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void editTask(Connection con) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Write your username:");
        String username = sc.nextLine();

        System.out.println("Write a task to edit: ");
        String taskToEdit = changeWordCase(sc.nextLine());

        System.out.println("Write how to edit the task:");
        System.out.println("1 - mark task as complete");
        System.out.println("2 - change name of the task");
        System.out.println("3 - change username");
        System.out.println("4 - change finish time");
        System.out.print("Your input: ");

        int inputNum = sc.nextInt();
        sc.nextLine();

        try {
            if(inputNum == 1) {
                PreparedStatement markAsComplete = con.prepareStatement("UPDATE todo_list " +
                        "SET status = ? " +
                        "WHERE task = ?");
                markAsComplete.setString(1, "✅");
                markAsComplete.setString(2, taskToEdit);

                int rowsEffected = markAsComplete.executeUpdate();

                if(rowsEffected > 0) System.out.println("Status changed successfully! ✅");
                else System.out.println("No rows effected \uD83D\uDEAB");

                markAsComplete.close();
            }
            else if(inputNum == 2) {
                System.out.println("Write a new name for the task:");
                String newTaskName = changeWordCase(sc.nextLine());

                PreparedStatement changeTaskName = con.prepareStatement("UPDATE todo_list " +
                        "SET task = ? " +
                        "WHERE task = ?");
                changeTaskName.setString(1, newTaskName);
                changeTaskName.setString(2, taskToEdit);

                int rowsEffected = changeTaskName.executeUpdate();

                if(rowsEffected > 0) System.out.println("Task name changed successfully! ✅");
                else System.out.println("No rows effected \uD83D\uDEAB");

                changeTaskName.close();
            }
            else if(inputNum == 3) {
                System.out.println("Write your new username:");
                String newUsername = sc.nextLine();

                PreparedStatement changeUsername = con.prepareStatement("UPDATE todo_list " +
                        "SET username = ? " +
                        "WHERE username = ?");
                changeUsername.setString(1, newUsername);
                changeUsername.setString(2, username);

                int rowsEffected = changeUsername.executeUpdate();

                if(rowsEffected > 0) System.out.println("Username changed successfully! ✅");
                else System.out.println("No rows effected \uD83D\uDEAB");

                changeUsername.close();
            }
            else if(inputNum == 4) {
                LocalDate currentDate = LocalDate.now();

                System.out.println("Write a new number of days until deadline:");
                int numberOfDaysToAdd = 0;

                try {
                    numberOfDaysToAdd = sc.nextInt();
                }
                catch (InputMismatchException e) {
                    throw new RuntimeException("Шо хоч се до сметрі робити?");
                }

                if(numberOfDaysToAdd < 0) throw new RuntimeException("Ти дебіл, як ти хочеш поставити від'ємне число днів?");

                LocalDate futureDate = currentDate.plusDays(numberOfDaysToAdd);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedFutureDate = futureDate.format(formatter);

                PreparedStatement changeFinishTime = con.prepareStatement("UPDATE todo_list " +
                        "SET finishtime = ? " +
                        "WHERE task = ?");
                changeFinishTime.setString(1, formattedFutureDate);
                changeFinishTime.setString(2, taskToEdit);

                int rowsEffected = changeFinishTime.executeUpdate();

                if(rowsEffected > 0) System.out.println("Finish time changed successfully! ✅");
                else System.out.println("No rows effected \uD83D\uDEAB");

                changeFinishTime.close();
            }
        }
        catch (SQLException e) {
            System.out.println("Something went wrong \uD83E\uDEE1");
        }
    }
    private static void viewTasksOfUser(Connection con) {
        LocalDate currDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedCurrDate = currDate.format(formatter);

        Scanner sc = new Scanner(System.in);

        System.out.println("Write your username:");
        String username = sc.nextLine();

        try {
            PreparedStatement selectTasksOfUser = con.prepareStatement(
                    "SELECT * FROM todo_list WHERE username = ?");
            selectTasksOfUser.setString(1, username);

            PreparedStatement countUncompletedTasks = con.prepareStatement("SELECT * FROM todo_list " +
                    "WHERE username = ? AND status = ?");
            countUncompletedTasks.setString(1, username);
            countUncompletedTasks.setString(2, "❌");

            PreparedStatement countOverdueTasks = con.prepareStatement("SELECT * FROM todo_list " +
                    "WHERE username = ? AND status = ? AND finishtime < ?");
            countOverdueTasks.setString(1, username);
            countOverdueTasks.setString(2, "❌");
            countOverdueTasks.setString(3, formattedCurrDate);

            ResultSet selectedTasks = selectTasksOfUser.executeQuery();
            ResultSet uncompletedTasks = countUncompletedTasks.executeQuery();
            ResultSet overdueTasks = countOverdueTasks.executeQuery();

            int totalTasks = 0;
            while(selectedTasks.next()) {
                totalTasks++;
                String task = selectedTasks.getString("task");
                String finishTime = selectedTasks.getString("finishtime");
                String status = selectedTasks.getString("status");

                System.out.println("Task: " + task + ", " + "Finish time: " + finishTime + ", "
                        + "Status: " + status);
            }

            int totalOverdueTasks = 0;
            while(overdueTasks.next()) {
                totalOverdueTasks++;
            }

            int totalUncompletedTasks = 0;
            while(uncompletedTasks.next()) {
                totalUncompletedTasks++;
            }

            System.out.println("Your total tasks: " + totalTasks);
            if(totalUncompletedTasks > 0) System.out.println("You have " + totalUncompletedTasks + " uncompleted tasks and " + totalOverdueTasks + " overdue tasks");
            System.out.println();

            con.close();
            selectedTasks.close();
            countUncompletedTasks.close();
            countOverdueTasks.close();
        } catch (SQLException e) {
//            System.out.println("Donno probably wrong input \uD83D\uDE1E\n");
            throw new RuntimeException();
        }
    }

    private static void deleteTask(Connection con) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Write your username: ");
        String username = sc.nextLine();

        System.out.print("Write a task that you want to delete: ");
        String taskToDelete = changeWordCase(sc.nextLine());
        try {
            PreparedStatement deleteTask = con.prepareStatement("DELETE FROM todo_list " +
                    "WHERE username = ? AND task = ?");
            deleteTask.setString(1, username);
            deleteTask.setString(2, taskToDelete);

            int rowsAffected = deleteTask.executeUpdate();

            // Check the result
            if (rowsAffected > 0) {
                System.out.println(rowsAffected + " row(s) deleted successfully.✅");
            } else {
                System.out.println("No rows deleted.\uD83D\uDEAB");
            }

            System.out.println();

            con.close();
            deleteTask.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String changeWordCase(String word) {
        String lowerCaseWord = word.toLowerCase();
        word = "" + word.substring(0, 1).toUpperCase() + lowerCaseWord.substring(1, lowerCaseWord.length());

        return word;
    }
}