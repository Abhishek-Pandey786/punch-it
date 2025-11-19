# Attendance Manager 📚

A smart desktop application for tracking class attendance with intelligent predictions and analytics. Built with JavaFX and SQLite.

![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![SQLite](https://img.shields.io/badge/SQLite-3.45-green)
![License](https://img.shields.io/badge/License-MIT-yellow)

## ✨ Features

### Core Features

- **Subject Management** - Add, edit, and delete subjects with customizable attendance targets
- **Real-Time Tracking** - Track attendance with Present/Absent/Cancelled status
- **Smart Dashboard** - Visual overview with color-coded progress bars and statistics
- **Intelligent Predictions** ⭐ - The standout feature:
  - Calculate exactly how many classes you need to attend to reach your target
  - Calculate how many classes you can safely bunk without falling below target
  - Real-time percentage predictions for future scenarios

### Advanced Features

- **Color-Coded Status**
  - 🟢 **Green (Safe)**: At or above target percentage
  - 🟡 **Yellow (Warning)**: Within 5% of target
  - 🔴 **Red (Risk)**: Below target threshold
- **Dark/Light Theme** - Toggle between themes for comfortable viewing
- **CSV Export** - Backup your attendance data
- **Local SQLite Database** - All data stored locally, works offline

## 🎯 Why This Project Stands Out

### Resume-Worthy Highlights

✅ **Algorithmic Thinking** - Smart attendance prediction algorithms showcase mathematical problem-solving  
✅ **Clean Architecture** - MVC pattern with proper separation (Model, View, Controller, Service, DAO layers)  
✅ **Modern UI/UX** - Polished JavaFX interface with custom CSS styling  
✅ **Database Integration** - Real persistence layer with SQLite  
✅ **Production-Ready** - Error handling, validation, and proper resource management

## 🚀 Getting Started

### Prerequisites

- **Java 17 or higher** ([Download](https://adoptium.net/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))

### Installation & Running

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd attendance-manager
   ```

2. **Build the project**

   ```bash
   mvn clean install
   ```

3. **Run the application**

   ```bash
   mvn javafx:run
   ```

   Or using the JavaFX plugin directly:

   ```bash
   mvn clean javafx:run
   ```

### Alternative: Run with JAR

```bash
# Build executable JAR
mvn clean package

# Run the JAR (Note: Requires JavaFX modules in classpath)
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar target/attendance-manager-1.0.0.jar
```

## 📖 How to Use

### 1. Add Your First Subject

- Click **"+ Add Subject"** button
- Enter:
  - Subject name (e.g., "Mathematics")
  - Total classes conducted so far
  - Classes you've attended
  - Target percentage (default: 75%)
- Click **Save**

### 2. View Smart Predictions

Each subject card displays:

- Current attendance percentage with color-coded status
- Progress bar showing visual representation
- **Intelligent recommendations**:
  - "Attend the next 2 classes to reach 75%"
  - "You can miss 3 more classes and still maintain 75%"
- Classes needed to reach target
- Safe bunks available

### 3. Track Daily Attendance

- Click **"Mark Attendance"** (Coming soon in future updates)
- Select date and subject
- Mark as Present/Absent/Cancelled
- Watch real-time updates to predictions

### 4. Export Your Data

- Click **"Export Data"** in the bottom toolbar
- Save as CSV file for backup or analysis

### 5. Switch Themes

- Click the 🌙/☀️ icon in the top-right
- Toggle between light and dark modes

## 🏗️ Project Structure

```
attendance-manager/
├── src/
│   ├── main/
│   │   ├── java/com/attendancemanager/
│   │   │   ├── App.java                    # Main application entry
│   │   │   ├── model/                      # Data models (Subject, AttendanceRecord)
│   │   │   ├── controller/                 # JavaFX controllers
│   │   │   ├── service/                    # Business logic & algorithms ⭐
│   │   │   ├── dao/                        # Database access layer
│   │   │   └── util/                       # Utilities (DatabaseManager)
│   │   └── resources/
│   │       └── com/attendancemanager/
│   │           ├── view/                   # FXML files
│   │           └── css/                    # Stylesheets
│   └── test/                               # Unit tests
├── pom.xml                                 # Maven configuration
├── attendance.db                           # SQLite database (created on first run)
└── README.md
```

## 🧮 Intelligent Algorithms Explained

### 1. Classes Needed to Reach Target

**Formula:**

```
x >= (target × total / 100 - attended) / (1 - target / 100)
```

Where `x` is the number of classes to attend.

**Example:**

- Current: 60/100 classes (60%)
- Target: 75%
- Need to attend: **20 classes**

### 2. Safe Bunks Calculator

**Formula:**

```
x <= (attended × 100 / target) - total
```

Where `x` is the number of classes you can miss.

**Example:**

- Current: 80/100 classes (80%)
- Target: 75%
- Can safely miss: **6 classes**

## 🛠️ Technology Stack

| Component        | Technology  | Purpose                      |
| ---------------- | ----------- | ---------------------------- |
| **Language**     | Java 17     | Core application logic       |
| **UI Framework** | JavaFX 21   | Modern desktop UI            |
| **Database**     | SQLite 3.45 | Local data persistence       |
| **Build Tool**   | Maven       | Dependency management        |
| **Architecture** | MVC/MVVM    | Clean separation of concerns |

## 📊 Database Schema

### Subjects Table

```sql
CREATE TABLE subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    total_classes INTEGER DEFAULT 0,
    attended_classes INTEGER DEFAULT 0,
    target_percentage REAL DEFAULT 75.0,
    color TEXT DEFAULT '#3498db',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Attendance Records Table

```sql
CREATE TABLE attendance_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    date DATE NOT NULL,
    status TEXT CHECK(status IN ('PRESENT', 'ABSENT', 'CANCELLED')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE(subject_id, date)
);
```

## 🔮 Future Enhancements

- [ ] **Daily Punch-In Interface** - Quick attendance marking for today
- [ ] **Calendar View** - Monthly calendar with attendance visualization
- [ ] **Attendance Streaks** - Track consecutive attendance days
- [ ] **Multiple Profiles** - Support for multiple students
- [ ] **Semester-wise Reports** - Generate PDF reports
- [ ] **Notifications** - Reminders when attendance is at risk
- [ ] **Cloud Sync** - Optional cloud backup (Firebase/AWS)
- [ ] **Mobile App** - Companion Android/iOS app

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Your Name**

- GitHub: [@yourusername](https://github.com/yourusername)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/yourprofile)

## 🙏 Acknowledgments

- Inspired by BunkMate and similar attendance tracking apps
- Icons and design influenced by modern Material Design principles
- Built as a portfolio project showcasing full-stack desktop development skills

## 📸 Screenshots

### Light Theme Dashboard

_Clean, modern interface with color-coded subject cards_

### Dark Theme

_Eye-friendly dark mode for late-night study sessions_

### Smart Predictions

_Intelligent recommendations based on mathematical algorithms_

---

⭐ **Star this repository** if you find it helpful!

**Built with ❤️ using Java and JavaFX**
