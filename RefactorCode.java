import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
final class Task {
    private final int id;
    private final String title;
    private final String description;
    private final LocalDate dueDate;
    private final String priority; // "Thấp" | "Trung bình" | "Cao"
    private final String status;   // "Chưa hoàn thành" | "Hoàn thành" (tuỳ bạn mở rộng)
    private final LocalDateTime createdAt;
    private final LocalDateTime lastUpdatedAt;

    Task(int id, String title, String description, LocalDate dueDate,
         String priority, String status, LocalDateTime createdAt, LocalDateTime lastUpdatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    int getId() { return id; }
    String getTitle() { return title; }
    String getDescription() { return description; }
    LocalDate getDueDate() { return dueDate; }
    String getPriority() { return priority; }
    String getStatus() { return status; }
    LocalDateTime getCreatedAt() { return createdAt; }
    LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }

    // ---- Serialization helpers ----
    static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ISO_DATE_TIME;

    @SuppressWarnings("unchecked")
    JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("id", id); // số nguyên
        obj.put("title", title);
        obj.put("description", description);
        obj.put("due_date", dueDate.format(DATE_FMT));
        obj.put("priority", priority);
        obj.put("status", status);
        obj.put("created_at", createdAt.format(DT_FMT));
        obj.put("last_updated_at", lastUpdatedAt.format(DT_FMT));
        return obj;
    }

    static Task fromJson(JSONObject obj) {
        int id = Integer.parseInt(obj.get("id").toString());
        String title = Objects.toString(obj.get("title"), "");
        String desc = Objects.toString(obj.get("description"), "");
        LocalDate due = LocalDate.parse(Objects.toString(obj.get("due_date")), DATE_FMT);
        String pri = Objects.toString(obj.get("priority"), "Thấp");
        String status = Objects.toString(obj.get("status"), "Chưa hoàn thành");
        LocalDateTime created = LocalDateTime.parse(Objects.toString(obj.get("created_at")), DT_FMT);
        LocalDateTime updated = LocalDateTime.parse(Objects.toString(obj.get("last_updated_at")), DT_FMT);
        return new Task(id, title, desc, due, pri, status, created, updated);
    }
}
class TaskRepository {
    private final Path dbPath;

    TaskRepository(String filePath) {
        this.dbPath = Paths.get(filePath);
    }

    List<Task> loadAll() {
        if (!Files.exists(dbPath)) {
            return new ArrayList<>();
        }
        JSONParser parser = new JSONParser();
        try (Reader reader = Files.newBufferedReader(dbPath, StandardCharsets.UTF_8)) {
            Object obj = parser.parse(reader);
            if (!(obj instanceof JSONArray)) {
                return new ArrayList<>();
            }
            JSONArray arr = (JSONArray) obj;
            List<Task> tasks = new ArrayList<>(arr.size());
            for (Object o : arr) {
                if (o instanceof JSONObject) {
                    tasks.add(Task.fromJson((JSONObject) o));
                }
            }
            return tasks;
        } catch (IOException | ParseException e) {
            System.err.println("[TaskRepository] Lỗi đọc DB: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    void saveAll(List<Task> tasks) {
        JSONArray arr = new JSONArray();
        for (Task t : tasks) {
            arr.add(t.toJson());
        }
        try (Writer writer = Files.newBufferedWriter(dbPath, StandardCharsets.UTF_8)) {
            writer.write(arr.toJSONString());
        } catch (IOException e) {
            System.err.println("[TaskRepository] Lỗi ghi DB: " + e.getMessage());
        }
    }
}
public class PersonalTaskManagerApp {
    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final String[] VALID_PRIORITIES = {"Thấp", "Trung bình", "Cao"};

    private final TaskRepository repo;
    private final List<Task> tasks; // dữ liệu trong bộ nhớ
    private int nextId = 1;

    public PersonalTaskManagerApp() {
        this.repo = new TaskRepository(DB_FILE_PATH);
        this.tasks = new ArrayList<>(repo.loadAll());
        // cập nhật nextId dựa trên dữ liệu hiện có
        for (Task t : tasks) {
            if (t.getId() >= nextId) {
                nextId = t.getId() + 1;
            }
        }
    }

    // ----- Public API -----
    public Task addTask(String title, String description, String dueDateStr, String priority) {
        // 1. Validate input
        LocalDate dueDate = validateInput(title, dueDateStr, priority);
        if (dueDate == null) {
            return null; // thông báo lỗi đã in trong validateInput
        }
        // 2. Kiểm tra trùng lặp
        if (existsTask(title, dueDate)) {
            System.out.println("Lỗi: Nhiệm vụ '" + title + "' đã tồn tại với cùng ngày đến hạn.");
            return null;
        }
        // 3. Tạo Task mới (status mặc định: Chưa hoàn thành)
        LocalDateTime now = LocalDateTime.now();
        Task newTask = new Task(nextId++, title.trim(), description == null ? "" : description.trim(),
                                dueDate, priority, "Chưa hoàn thành", now, now);
        // 4. Cập nhật bộ nhớ & ghi DB
        tasks.add(newTask);
        repo.saveAll(tasks);
        System.out.println("Đã thêm nhiệm vụ thành công (ID=" + newTask.getId() + ")");
        return newTask;
    }

    public List<Task> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    // ----- Helpers -----
    private LocalDate validateInput(String title, String dueDateStr, String priority) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Lỗi: Tiêu đề không được để trống.");
            return null;
        }
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            System.out.println("Lỗi: Ngày đến hạn không được để trống.");
            return null;
        }
        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateStr, Task.DATE_FMT);
        } catch (DateTimeParseException e) {
            System.out.println("Lỗi: Ngày đến hạn không hợp lệ. Định dạng phải YYYY-MM-DD.");
            return null;
        }
        if (!Arrays.asList(VALID_PRIORITIES).contains(priority)) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ. Chọn Thấp/Trung bình/Cao.");
            return null;
        }
        return dueDate;
    }

    private boolean existsTask(String title, LocalDate dueDate) {
        for (Task t : tasks) {
            if (t.getTitle().equalsIgnoreCase(title.trim()) && t.getDueDate().equals(dueDate)) {
                return true;
            }
        }
        return false;
    }

    // ----- Demo main -----
    public static void main(String[] args) {
        PersonalTaskManagerApp manager = new PersonalTaskManagerApp();

        System.out.println("\nThêm nhiệm vụ hợp lệ:");
        manager.addTask("Mua sách", "Sách Công nghệ phần mềm.", "2025-07-20", "Cao");

        System.out.println("\nThử thêm nhiệm vụ trùng lặp:");
        manager.addTask("Mua sách", "Bản khác.", "2025-07-20", "Cao");

        System.out.println("\nThêm nhiệm vụ khác:");
        manager.addTask("Tập thể dục", "Tập gym 1 tiếng.", "2025-07-21", "Trung bình");

        System.out.println("\nThêm nhiệm vụ với tiêu đề rỗng (sẽ lỗi):");
        manager.addTask("", "Nhiệm vụ không có tiêu đề.", "2025-07-22", "Thấp");

        System.out.println("\nDanh sách nhiệm vụ hiện có:");
        for (Task t : manager.getAllTasks()) {
            System.out.println("- [" + t.getId() + "] " + t.getTitle() + " (" + t.getDueDate() + ", " + t.getPriority() + ")");
        }
    }
}
