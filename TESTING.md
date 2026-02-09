# Testing Guide

This project uses [Kotest](https://kotest.io/) for unit testing.

## Running Tests

### All Tests
```bash
./gradlew test
```

### Specific Platform Tests
```bash
# JVM/Desktop Tests
./gradlew jvmTest

# Android Tests
./gradlew testDebugUnitTest

# iOS Tests (requires macOS)
./gradlew iosSimulatorArm64Test
```

### Watch Mode (continuous testing)
```bash
./gradlew test --continuous
```

## Current Test Coverage

### Unit Tests
- **ComposeAppCommonTest**: Basic smoke tests
- **ZipExportImportTest**: ZIP file export/import functionality tests
  - Full roundtrip (export → import → verify)
  - Empty directory handling
  - Binary data preservation

### Test Framework
We use **Kotest 5.9.1** with the FunSpec style:
- More readable syntax (`shouldBe`, `shouldNotBeEmpty`)
- Better error messages
- Kotlin-first design
- MIT-compatible (Apache 2.0 license)

## Writing Tests

### Basic Test Structure
```kotlin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MyTest : FunSpec({
    test("description of what is being tested") {
        val result = 1 + 2
        result shouldBe 3
    }
})
```

### Common Matchers
```kotlin
// Equality
result shouldBe expected

// Booleans
condition.shouldBeTrue()
condition.shouldBeFalse()

// Nullability
value.shouldNotBeNull()
value.shouldBeNull()

// Collections
list.shouldNotBeEmpty()
list shouldContain "item"
list.size shouldBe 5

// Strings
text shouldStartWith "Hello"
text shouldContain "world"
```

### Async Tests
Tests support coroutines out of the box:
```kotlin
test("async operation") {
    val result = withContext(Dispatchers.IO) {
        // async operation
    }
    result shouldBe expected
}
```

## Test File Locations
```
composeApp/
├── src/
│   ├── commonMain/        # Production code
│   └── commonTest/        # Shared tests (all platforms)
│       └── kotlin/
│           └── org/example/project/
│               ├── ComposeAppCommonTest.kt
│               └── ZipExportImportTest.kt
```

## CI/CD Integration
Tests run automatically on:
- `./gradlew build` (includes all tests)
- Before creating releases
- In CI pipelines (if configured)

## Test Philosophy
- **Fast**: Unit tests should run quickly
- **Isolated**: Each test should be independent
- **Clear**: Test names should describe what is tested
- **Maintainable**: Keep tests simple and readable

## Adding New Tests
1. Create a new `*Test.kt` file in `commonTest/kotlin/`
2. Extend `FunSpec`
3. Use descriptive test names
4. Use Kotest matchers for assertions
5. Run tests: `./gradlew test`

## Debugging Tests
```bash
# Verbose output
./gradlew test --info

# Show test output
./gradlew test --rerun-tasks

# Debug specific test
./gradlew test --tests "org.example.project.ZipExportImportTest"
```

## Resources
- [Kotest Documentation](https://kotest.io/)
- [Kotest Matchers](https://kotest.io/docs/assertions/matchers.html)
- [Kotlin Multiplatform Testing](https://kotlinlang.org/docs/multiplatform-run-tests.html)
