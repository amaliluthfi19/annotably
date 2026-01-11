# Dependency Injection with Hilt

This project uses **Hilt** (Google's recommended dependency injection library for Android) to manage dependencies.

## Table of Contents
- [Setup Overview](#setup-overview)
- [Key Components](#key-components)
- [How to Use](#how-to-use)
- [Adding New Dependencies](#adding-new-dependencies)
- [Best Practices](#best-practices)

## Setup Overview

The project is configured with the following Hilt components:

1. **Application Class** (`AnnotablyApplication.kt`) - Entry point for Hilt
2. **Modules** - Define how to provide dependencies
   - `NetworkModule` - Provides network-related dependencies (Retrofit, OkHttp, ApiService)
   - `RepositoryModule` - Provides repository dependencies
3. **ViewModels** - Annotated with `@HiltViewModel` for automatic injection
4. **Activities** - Annotated with `@AndroidEntryPoint` to enable injection

## Key Components

### 1. Application Class

```kotlin
@HiltAndroidApp
class AnnotablyApplication : Application()
```

The `@HiltAndroidApp` annotation triggers Hilt's code generation and must be placed on your Application class.

### 2. MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Your activity code
}
```

The `@AndroidEntryPoint` annotation enables field injection in Android classes.

### 3. Network Module

Located in `di/NetworkModule.kt`, this module provides:
- `HttpLoggingInterceptor` - For logging network requests/responses
- `OkHttpClient` - Configured HTTP client
- `Retrofit` - Retrofit instance
- `ApiService` - Your API service interface

All network dependencies are **Singletons** (one instance throughout the app lifecycle).

### 4. Repository Module

Located in `di/RepositoryModule.kt`, this module provides:
- `ExampleRepository` - Your repository implementation

### 5. ViewModels

```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    // Inject your dependencies here
    // private val repository: YourRepository
) : ViewModel() {
    // Your ViewModel logic
}
```

### 6. Repositories

```kotlin
class ExampleRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {
    // Your repository methods
}
```

## How to Use

### Injecting Dependencies in ViewModels

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val myRepository: MyRepository,
    private val anotherDependency: AnotherDependency
) : ViewModel() {
    
    fun fetchData() {
        viewModelScope.launch {
            val result = myRepository.getData()
            // Handle result
        }
    }
}
```

### Using ViewModels in Composables

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    // Use viewModel
}
```

Hilt automatically provides the ViewModel with all its dependencies injected.

### Injecting Dependencies in Repositories

```kotlin
class MyRepository @Inject constructor(
    private val apiService: ApiService,
    private val someOtherDependency: SomeOtherDependency
) : BaseRepository() {
    
    suspend fun getData(): NetworkResult<MyData> {
        return safeApiCall {
            apiService.getMyData()
        }
    }
}
```

## Adding New Dependencies

### 1. Adding a New Repository

**Step 1:** Create your repository with constructor injection:

```kotlin
class UserRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {
    
    suspend fun getUsers(): NetworkResult<List<User>> {
        return safeApiCall {
            apiService.getUsers()
        }
    }
}
```

**Step 2:** Add it to `RepositoryModule.kt` (optional, only if you need custom configuration):

```kotlin
@Provides
@Singleton
fun provideUserRepository(apiService: ApiService): UserRepository {
    return UserRepository(apiService)
}
```

**Note:** If your repository only has `@Inject` constructor, you don't need to add it to a module. Hilt can automatically provide it.

### 2. Adding a New Module

If you need to provide dependencies that can't use constructor injection:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
```

### 3. Creating a New ViewModel

```kotlin
@HiltViewModel
class NewViewModel @Inject constructor(
    private val repository: YourRepository,
    // Add more dependencies as needed
) : ViewModel() {
    
    // Your ViewModel logic
}
```

### 4. Providing Context

To inject application context:

```kotlin
class MyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) : BaseRepository()
```

## Best Practices

### 1. Use Constructor Injection When Possible

Constructor injection is preferred because it makes dependencies explicit and easier to test:

```kotlin
class MyRepository @Inject constructor(
    private val apiService: ApiService
)
```

### 2. Scope Your Dependencies Appropriately

- `@Singleton` - One instance for the entire app lifecycle (use for repositories, API services)
- `@ViewModelScoped` - One instance per ViewModel lifecycle
- `@ActivityScoped` - One instance per Activity lifecycle

### 3. Keep Modules Organized

Group related dependencies together:
- `NetworkModule` - All network-related dependencies
- `DatabaseModule` - All database-related dependencies
- `RepositoryModule` - Repository bindings

### 4. Use Interfaces for Testability

Define repository interfaces to make testing easier:

```kotlin
interface UserRepository {
    suspend fun getUsers(): NetworkResult<List<User>>
}

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository, BaseRepository() {
    override suspend fun getUsers(): NetworkResult<List<User>> {
        return safeApiCall { apiService.getUsers() }
    }
}
```

Then in your module:

```kotlin
@Binds
abstract fun bindUserRepository(
    impl: UserRepositoryImpl
): UserRepository
```

### 5. Avoid Field Injection in New Code

Prefer constructor injection over field injection:

❌ **Avoid:**
```kotlin
@AndroidEntryPoint
class MyActivity : AppCompatActivity() {
    @Inject
    lateinit var myDependency: MyDependency
}
```

✅ **Prefer:**
```kotlin
class MyClass @Inject constructor(
    private val myDependency: MyDependency
)
```

## Testing with Hilt

For testing, you can replace Hilt modules with test modules:

```kotlin
@HiltAndroidTest
class MyTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Test
    fun myTest() {
        // Your test
    }
}
```

## Additional Resources

- [Hilt Official Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Hilt Codelab](https://developer.android.com/codelabs/android-hilt)
- [Dependency Injection Best Practices](https://developer.android.com/training/dependency-injection)
