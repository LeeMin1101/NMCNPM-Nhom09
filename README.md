# 📑 PersonalTaskManager (Java 17)

> **Ngôn ngữ:** Java  
> **Yêu cầu:** JDK 11 trở lên (khuyên dùng 17)  
> **Phụ thuộc ngoài:** **không** – chỉ dùng thư viện chuẩn JDK  
> **Lưu trữ dữ liệu:** tệp văn bản `tasks.txt`

---

## 1. Mục đích dự án
PersonalTaskManager là chương trình **dòng lệnh** siêu gọn giúp bạn:

* Thêm nhiệm vụ cá nhân (Task) với tiêu đề, mô tả, ngày đến hạn và mức độ ưu tiên.  
* Tự kiểm tra trùng lặp tiêu đề + ngày đến hạn.  
* Hiển thị toàn bộ danh sách nhiệm vụ theo định dạng dễ đọc.  
* Lưu & nạp dữ liệu từ tệp `tasks.txt` ngay trong thư mục chạy.

Dự án minh họa cách áp dụng **KISS / DRY / YAGNI**:  
không thư viện ngoài, cấu trúc một lớp, dữ liệu thô dạng “|” – đủ dùng mà cực kỳ dễ hiểu.

---

## 2. Công nghệ & cấu trúc chính
| Phần | Chi tiết |
|------|----------|
| **Ngôn ngữ** | Java chuẩn (không cần Maven/Gradle) |
| **Gói import** | `java.io.*`, `java.time.*`, `java.util.*` |
| **Tệp dữ liệu** | `tasks.txt` – mỗi dòng là một Task, các trường phân tách bằng ký tự `\|` |
| **Lớp chính** | `PersonalTaskManager` |
| **Lớp nội tại** | `static class Task` – ánh xạ 1 dòng tệp ↔ 1 đối tượng |
| **Định dạng ngày** | `yyyy-MM-dd` qua `DateTimeFormatter` |
| **Ưu tiên hợp lệ** | `"Thap"`, `"Trung binh"`, `"Cao"` |
| **Nguyên tắc thiết kế** | *KISS* (1 class, 1 tệp), *DRY* (hàm `loadTasks` / `saveTasks` dùng chung), *YAGNI* (không thêm tính năng chưa cần như UUID hay JSON) |

---

## 3. Cách hoạt động (luồng chính)

```mermaid
flowchart TD
    A[Start] --> B[Khởi tạo PersonalTaskManager]
    B --> C{Có file tasks.txt?}
    C -- Có --> D[Tải từng dòng vào List<Task>]
    C -- Không --> E[Tạo List rỗng]
    D --> F[Tính nextId = max(id)+1]
    E --> F
    F --> G[addTask(...)]:::blue
    G --> H{Tiêu đề & ưu tiên hợp lệ?}
    H -- Không --> I[Ném IllegalArgumentException]
    H -- Có --> J{Có task trùng?}
    J -- Có --> I
    J -- Không --> K[Tạo Task mới, nextId++]
    K --> L[Lưu List xuống file]
    L --> M[Hiển thị Task mới & tiếp tục]
    classDef blue fill:#caf,stroke:#336;
```

* **loadTasks()** Đọc tệp; dòng nhập lỗi bị bỏ qua → khởi tạo nhanh.  
* **addTask(…)** Gọi chuỗi kiểm tra hợp lệ → tạo `Task` → ghi đè file bằng toàn bộ danh sách.  
* **showAllTasks()** In từng task theo định dạng:  
  `ID:x | <Tiêu đề> | <yyyy-MM-dd> | <Ưu tiên>`  

---

## 4. Hướng dẫn biên dịch & chạy

```bash
# 1. Lưu mã vào PersonalTaskManager.java
# 2. Biên dịch
javac PersonalTaskManager.java

# 3. Chạy (tạo/tải tasks.txt tự động)
java PersonalTaskManager
```

*Chú ý*: Nếu `tasks.txt` chưa tồn tại, chương trình sẽ tạo mới khi thêm Task đầu tiên.

---

## 5. Định dạng dữ liệu trong `tasks.txt`

```
<id>|<title>|<description>|<yyyy-MM-dd>|<priority>|<status>
```

Ví dụ:

```
1|Lap ke hoach cho Do an mon hoc|Phan chia cong viec 3 tuan|2025-07-24|Cao|Chua hoan thanh
```

---

## 6. Mở rộng gợi ý

| Ý tưởng | Ghi chú |
|---------|---------|
| Sửa/Xóa Task | Thêm `updateTask`, `deleteTask` – tái dùng `saveTasks()` |
| Lọc theo ưu tiên | Duyệt `tasks` + `Stream.filter` |
| CSV/JSON | Thay `tasks.txt` bằng JSON/Jackson (khi thật sự cần) |
| Giao diện Swing/JavaFX | Bọc API hiện có – logic không đổi |

---

## 7. Giấy phép
Mã nguồn phát hành theo **MIT License** – thoải mái sửa, chia sẻ, trích dẫn.

---
