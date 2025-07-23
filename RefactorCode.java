import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PersonalTaskManager {
    private static final String DB_FILE = "tasks.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Set<String> PRIORITIES = Set.of("Thap", "Trung binh", "Cao");
    
    private List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    static class Task {
        int id;
        String title, description, priority, status;
        LocalDate dueDate;
        LocalDateTime created;

        Task(int id, String title, String desc, LocalDate due, String priority) {
            this.id = id;
            this.title = title;
            this.description = desc;
            this.dueDate = due;
            this.priority = priority;
            this.status = "Chua hoan thanh";
            this.created = LocalDateTime.now();
        }

        Task(String line) {
            String[] parts = line.split("\\|");
            this.id = Integer.parseInt(parts[0]);
            this.title = parts[1];
            this.description = parts[2];
            this.dueDate = LocalDate.parse(parts[3]);
            this.priority = parts[4];
            this.status = parts[5];
        }

        String toLine() {
            return id + "|" + title + "|" + description + "|" + dueDate + "|" + priority + "|" + status;
        }

        @Override
        public String toString() {
            return String.format("ID:%d | %s | %s | %s", id, title, dueDate, priority);
        }
    }

    public PersonalTaskManager() {
        loadTasks();
        nextId = tasks.stream().mapToInt(t -> t.id).max().orElse(0) + 1;
    }

    private void loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DB_FILE))) {
            reader.lines().forEach(line -> {
                try { tasks.add(new Task(line)); } 
                catch (Exception ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private void saveTasks() {
        try (PrintWriter writer = new PrintWriter(DB_FILE)) {
            tasks.forEach(task -> writer.println(task.toLine()));
        } catch (IOException e) {
            throw new RuntimeException("Loi luu file: " + e.getMessage());
        }
    }

    public Task addTask(String title, String description, String dateStr, String priority) {
        if (title == null || title.trim().isEmpty()) 
            throw new IllegalArgumentException("Tieu de khong duoc trong");
        if (!PRIORITIES.contains(priority)) 
            throw new IllegalArgumentException("Priority khong hop le");
        
        LocalDate dueDate = LocalDate.parse(dateStr, DATE_FORMAT);
        
        if (tasks.stream().anyMatch(t -> t.title.equalsIgnoreCase(title) && t.dueDate.equals(dueDate)))
            throw new IllegalArgumentException("Task da ton tai");

        Task newTask = new Task(nextId++, title, description, dueDate, priority);
        tasks.add(newTask);
        saveTasks();
        
        System.out.println("Da them: " + newTask);
        return newTask;
    }

    public void showAllTasks() {
        if (tasks.isEmpty()) {
            System.out.println("Khong co task nao");
            return;
        }
        System.out.println("\nDANH SACH TASKS:");
        tasks.forEach(System.out::println);
    }

    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();
        
        try {
            manager.addTask("Lap ke hoach cho Do an mon hoc", "Phan chia cong viec 3 tuan", "2025-07-24", "Cao");
            manager.addTask("Thu thap yeu cau ung dung Quan ly Nhiem vu Ca nhan", "Viet User Story va Product Backlog", "2025-07-25", "Cao");
            manager.addTask("Thiet ke", "Ve Context Diagram cho he thong", "2025-07-26", "Trung binh");
            manager.addTask("Code theo chuan", "Refactor code theo nguyen tac KISS/SOLID", "2025-07-27", "Cao");
            manager.addTask("Test", "Viet Function Test Case cho ung dung", "2025-07-28", "Trung binh");
            manager.addTask("Van dung Cong cu quan ly du an", "Su dung tool de theo doi tien do", "2025-07-29", "Thap");
            
            manager.showAllTasks();
            
            // Test trung lap
            manager.addTask("Lap ke hoach cho Do an mon hoc", "Mo ta khac", "2025-07-24", "Thap");
        } catch (Exception e) {
            System.err.println("Loi: " + e.getMessage());
        }
    }
}
