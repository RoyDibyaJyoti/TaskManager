# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-06-02

### Added
- **Interactive Dashboard**: Real-time cards showing total, completed, pending, and overdue tasks. Features a progress bar for completion rate, along with custom visual breakdown bars by category and priority.
- **Dynamic Task Management**: Create, edit, toggle, and delete tasks. Columns support sorting, category color circles, and priority badges.
- **Inline Task Actions**: Mark tasks completed/pending directly using checkboxes inside the TableView.
- **Sophisticated Filters**: Dynamically filter tasks as you type or change dropdowns (Category, Priority, Completion Status).
- **Category Customization**: Create custom categories with a color picker and delete existing ones. Deleted categories cascade-update tasks to "Uncategorized".
- **Due Dates & Overdue Alerts**: Color-coded date indicators (Red for overdue tasks, Orange for tasks due today).
- **Light & Dark Mode**: Instantly switch visual themes. Dark mode overrides design tokens dynamically at runtime without requiring an app restart.
- **Local JSON Persistence**: Automatic background saving/loading to platform-specific data folders:
  - **macOS**: `~/Library/Application Support/TaskManager/`
  - **Windows**: `%APPDATA%\TaskManager\`
  - **Linux**: `~/.config/TaskManager/`
- **Native macOS HIG Integration**:
  - System menu bar (`useSystemMenuBar="true"`).
  - Native accelerators mapping `Shortcut` (Command key on macOS).
  - Custom brand About and Preferences dialogs integrated directly with Apple application menu items.
  - Native taskbar and Dock naming option (`-Xdock:name=TaskManager`).
- **Crisp Vector SVG Icons**: Visual SVG indicators on sidebar navigation and toolbar actions that automatically transition colors.
- **CI/CD Automated Workflows**: Multi-platform GitHub Actions building native installers:
  - `.dmg` for macOS
  - `.msi` and `.exe` for Windows
  - `.deb` and `.rpm` for Linux

### Fixed
- Fixed TableView row duplicates and empty virtual border lines using transparent row styling.
- Resolved memory leaks and NullPointerExceptions on column rendering by adding defensive assertions.
- Fixed FXML layout jumping by pre-allocating spaces for validation labels.
- Resolved workspace relative path issues inside macOS `.app` bundles by refactoring to platform-specific path resolving.
