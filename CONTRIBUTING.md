# Contributing to TaskManager

First off, thank you for taking the time to contribute! Contributions are what make the open-source community such an amazing place to learn, inspire, and create.

---

## Code of Conduct

By participating in this project, you are expected to uphold our [Code of Conduct](CODE_OF_CONDUCT.md).

---

## How Can I Contribute?

### Reporting Bugs
- Check the [Issues Tab](https://github.com/roydibyajyoti/TaskManager/issues) to ensure the bug hasn't been reported yet.
- Open a new issue, describing the bug, steps to reproduce, expected behavior, and screenshots if applicable.

### Suggesting Enhancements
- Check current open enhancements or features.
- Open an issue describing the proposed feature, why it is useful, and potential implementation details.

### Submitting Pull Requests
1. Fork the repository and create your branch from `main`:
   ```bash
   git checkout -b feature/my-new-feature
   ```
2. Set up your local development environment:
   - JDK 17 or higher
   - Maven 3.6+
3. Write clean, SOLID code. Follow Java coding standards.
4. Ensure your changes compile and pass all automated tests:
   ```bash
   mvn clean test
   ```
5. Commit your changes with descriptive messages:
   ```bash
   git commit -m "feat: add support for task archiving"
   ```
6. Push to your branch and open a Pull Request against `main`.

---

## Coding Conventions

- **Indentation**: Use 4 spaces for Java, 4 spaces for XML (FXML).
- **Naming**:
  - Class names: `PascalCase` (e.g. `MainController`)
  - Method/Variable names: `camelCase` (e.g. `applyFilters`)
  - Constants: `UPPER_SNAKE_CASE` (e.g. `DEFAULT_PRIORITY`)
- **FXML**: Define controller fields and event handler bindings cleanly. Avoid inline styles in FXML; define classes in `style.css` and use `styleClass`.
- **Tests**: Write unit and integration tests under `src/test/java/` for new services and utilities.
