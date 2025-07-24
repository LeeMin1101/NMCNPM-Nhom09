import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManagerCleaned {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JSONArray loadTasksFromDb() {
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);
            return (JSONArray) obj;
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi đọc database: " + e.getMessage());
            return new JSONArray();
        }
    }

    private void saveTasksToDb(JSONArray tasksData) {
        try (FileWriter writer = new FileWriter(DB_FILE_PATH)) {
            writer.write(tasksData.toJSONString());
            writer.flush();
        } catch (IOException e) {
            System.err.println("Lỗi ghi file database: " + e.getMessage());
        }
    }

    private boolean isValidPriority(String priority) {
        return priority.equals("Thấp") || priority.equals("Trung bình") || priority.equals("Cao");
    }

    private boolean isDuplicateTask(JSONArray tasks, String title, String dueDateStr) {
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (task.get("title").toString().equalsIgnoreCase(title)
                    && task.get("due_date").toString().equals(dueDateStr)) {
                return true;
            }
        }
        return false;
    }

    public JSONObject addNewTask(String title, String description,
                                 String dueDateStr, String priorityLevel, boolean isRecurring) {

        if (title == null || title.trim().isEmpty()) {
            System.out.println("❌ Tiêu đề không được để trống.");
            return null;
        }

        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            System.out.println("❌ Ngày đến hạn không được để trống.");
            return null;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("❌ Ngày đến hạn sai định dạng. Đúng: YYYY-MM-DD.");
            return null;
        }

        if (!isValidPriority(priorityLevel)) {
            System.out.println("❌ Mức độ ưu tiên không hợp lệ. Chỉ chấp nhận: Thấp, Trung bình, Cao.");
            return null;
        }

        JSONArray tasks = loadTasksFromDb();

        if (isDuplicateTask(tasks, title, dueDateStr)) {
            System.out.printf("❌ Nhiệm vụ '%s' đã tồn tại với cùng ngày đến hạn.\n", title);
            return null;
        }

        JSONObject newTask = new JSONObject();
        String taskId = UUID.randomUUID().toString();
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        newTask.put("id", taskId);
        newTask.put("title", title);
        newTask.put("description", description);
        newTask.put("due_date", dueDate.format(DATE_FORMATTER));
        newTask.put("priority", priorityLevel);
        newTask.put("status", "Chưa hoàn thành");
        newTask.put("created_at", now);
        newTask.put("last_updated_at", now);

        if (isRecurring) {
            newTask.put("is_recurring", true);
            newTask.put("recurrence_pattern", "Chưa xác định"); // Có thể mở rộng sau
        }

        tasks.add(newTask);
        saveTasksToDb(tasks);

        System.out.printf("✅ Đã thêm nhiệm vụ thành công. ID: %s\n", taskId);
        return newTask;
    }

    public static void main(String[] args) {
        PersonalTaskManagerCleaned manager = new PersonalTaskManagerCleaned();

        manager.addNewTask("Mua sách", "Sách Công nghệ phần mềm.", "2025-07-20", "Cao", false);
        manager.addNewTask("Mua sách", "Trùng tiêu đề và ngày.", "2025-07-20", "Cao", false);
        manager.addNewTask("Tập thể dục", "Tập gym 1 tiếng.", "2025-07-21", "Trung bình", true);
        manager.addNewTask("", "Không có tiêu đề.", "2025-07-22", "Thấp", false);
    }
}
