# Contributing to APNG Kotlin Multiplatform Library

Thank you for your interest in contributing to the APNG library! We welcome contributions from the community. Please read these guidelines to understand how to contribute effectively.

## Code of Conduct

Be respectful and constructive in all interactions. We are committed to providing a welcoming and inclusive environment for all contributors.

## Getting Started

### Prerequisites

- Kotlin 2.3.0 or later
- Android SDK 24+
- Gradle 8.11.2 or later
- Java 11+

### Setting Up the Development Environment

1. Fork the repository
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/apng.git
   cd apng
   ```
3. Create a local configuration file for your development environment:
   ```bash
   cp local.properties.example local.properties
   # Edit local.properties with your SDK path
   ```
4. Build the project:
   ```bash
   ./gradlew build
   ```

## Contribution Workflow

### 1. Create a Feature Branch

Create a new branch for your changes:

```bash
git checkout -b feature/your-feature-name
# or for bug fixes:
git checkout -b fix/your-bug-name
```

### 2. Make Your Changes

- Follow the code style guidelines (see below)
- Write clear, descriptive commit messages
- Add or update tests as needed
- Update documentation if required

### 3. Testing

Before submitting your changes, ensure all tests pass:

```bash
./gradlew test
```

For platform-specific testing:

```bash
# Android
./gradlew :apng-core:testDebug

# Desktop
./gradlew :apng-core:desktopTest

# Run demo app
./gradlew :composeApp:run
```

### 4. Commit Your Changes

Write clear, descriptive commit messages:

```bash
git commit -m "feat: add feature description" -m "Detailed explanation of the change"
```

Commit message conventions:
- `feat:` for new features
- `fix:` for bug fixes
- `docs:` for documentation changes
- `style:` for code style improvements
- `refactor:` for code refactoring
- `test:` for test additions/changes
- `chore:` for maintenance tasks

### 5. Push and Create a Pull Request

Push your branch to your fork:

```bash
git push origin feature/your-feature-name
```

Then open a Pull Request on the main repository with:
- Clear description of what changes you made
- Reference to any related issues (#issue-number)
- Screenshots or examples if applicable
- Test results

## Code Style Guidelines

### Kotlin Style

We follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

- Use 4 spaces for indentation
- Max line length: 120 characters (soft), 140 (hard)
- Use meaningful names for variables and functions
- Write comprehensive KDoc comments for public APIs

### Example

```kotlin
/**
 * Loads an APNG image from a URL with automatic caching.
 *
 * @param url the URL to load from
 * @param onProgress callback for progress updates (downloaded, total bytes)
 * @return the loaded APNG image
 * @throws IOException if the network request fails
 */
suspend fun loadFromUrl(
    url: String,
    onProgress: ((Long, Long) -> Unit)? = null
): ApngImage = ...
```

### Best Practices

- Use `expect`/`actual` for platform-specific code
- Prefer immutability (use `data class` and `sealed class`)
- Use coroutines for async operations
- Add proper error handling with custom exceptions
- Keep functions small and focused

## Documentation

- Update README.md if your change affects public APIs or usage
- Add KDoc comments to all public functions and classes
- Include examples for new features
- Update version information if needed

## Reporting Issues

When reporting bugs, please include:

1. A clear description of the issue
2. Steps to reproduce the problem
3. Expected behavior vs. actual behavior
4. Platform and version information (Android, iOS, Desktop, Web)
5. Code snippet that demonstrates the issue
6. Any error messages or logs

Example issue template:

```markdown
**Description:** Brief description of the bug

**Platform:** Android (API 31) / iOS / Desktop / Web

**Steps to Reproduce:**
1. ...
2. ...
3. ...

**Expected Behavior:** 
What should happen

**Actual Behavior:**
What actually happens

**Code:**
\`\`\`kotlin
// Your code here
\`\`\`

**Logs/Error Messages:**
\`\`\`
...
\`\`\`
```

## Feature Requests

Feature requests are welcome! Please:

1. Clearly describe the feature and its use case
2. Explain how it benefits the library
3. Provide examples of how it would be used
4. Consider implementation complexity

## Pull Request Guidelines

- Keep PRs focused on a single feature or fix
- Break large changes into multiple smaller PRs
- Ensure CI/CD checks pass
- Respond to code review feedback promptly
- Be respectful and constructive in discussions

## Project Structure

```
apng/
├── apng-core/           # Core APNG parsing (platform-independent)
├── apng-compose/        # Compose UI components
├── apng-network-core/   # Network infrastructure
├── apng-network/        # Network implementation
├── apng-resources/      # Resource loading
├── composeApp/          # Demo application
└── gradle/              # Build configuration
```

### Key Components

- **apng-core**: Core APNG parsing and rendering logic
  - No UI dependencies
  - Platform-specific decoders via `expect/actual`
  - Comprehensive error handling

- **apng-compose**: Jetpack Compose integration
  - Composable UI components
  - State management
  - Cross-platform support

- **apng-network**: Network loading and caching
  - HTTP client interface
  - LRU disk cache
  - Automatic retry logic

## Licensing

By contributing to this project, you agree that your contributions will be licensed under the MIT License.

## Questions?

Feel free to ask questions by:

1. Opening a Discussion in the repository
2. Creating an Issue with the `question` label
3. Reaching out to the maintainers

---

Thank you for contributing to make this library better!
