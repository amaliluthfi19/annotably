# Hilt DI - Quick Reference

## Essential Annotations

| Annotation | Where to Use | Purpose |
|------------|--------------|---------|
| `@HiltAndroidApp` | Application class | Triggers Hilt code generation |
| `@AndroidEntryPoint` | Activity, Fragment, View, Service | Enables field injection |
| `@HiltViewModel` | ViewModel | Marks ViewModel for injection |
| `@Inject` | Constructor | Tells Hilt how to provide instances |
| `@Module` | Object/Abstract class | Defines how to provide dependencies |
| `@InstallIn` | Module | Specifies component lifetime |
| `@Provides` | Function in module | Provides a dependency |
| `@Binds` | Abstract function | Binds interface to implementation |
| `@Singleton` | Provides function | One instance for app lifetime |

## Common Patterns

### 1. Injectable Class
```kotlin
class MyRepository @Inject constructor(
    private val apiService: ApiService
)
```

### 2. ViewModel
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel()
```

### 3. Use ViewModel in Composable
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) { }
```

### 4. Provide Interface Implementation
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindRepository(
        impl: MyRepositoryImpl
    ): MyRepository
}
```

### 5. Provide Dependency That Needs Configuration
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "database"
        ).build()
    }
}
```

### 6. Inject Application Context
```kotlin
class MyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
)
```

### 7. Named Dependencies
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    @Named("baseUrl")
    fun provideBaseUrl(): String = "https://api.example.com/"
    
    @Provides
    @Singleton
    fun provideRetrofit(
        @Named("baseUrl") baseUrl: String
    ): Retrofit { }
}

// Usage
@Inject constructor(
    @Named("baseUrl") private val baseUrl: String
)
```

### 8. Qualifier Annotations (Better than @Named)
```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoggingInterceptor

// In module
@Provides
@AuthInterceptor
fun provideAuthInterceptor(): Interceptor { }

@Provides
@LoggingInterceptor
fun provideLoggingInterceptor(): Interceptor { }

// Usage
@Inject constructor(
    @AuthInterceptor private val authInterceptor: Interceptor,
    @LoggingInterceptor private val loggingInterceptor: Interceptor
)
```

## Component Scopes

| Component | Scope | Lifetime |
|-----------|-------|----------|
| SingletonComponent | @Singleton | Application |
| ActivityRetainedComponent | @ActivityRetainedScoped | Activity (survives config changes) |
| ViewModelComponent | @ViewModelScoped | ViewModel |
| ActivityComponent | @ActivityScoped | Activity |
| FragmentComponent | @FragmentScoped | Fragment |
| ViewComponent | @ViewScoped | View |
| ServiceComponent | @ServiceScoped | Service |

## Module Installation

```kotlin
@Module
@InstallIn(SingletonComponent::class)  // Lives as long as application
object NetworkModule { }

@Module
@InstallIn(ViewModelComponent::class)  // Lives as long as ViewModel
object ViewModelModule { }

@Module
@InstallIn(ActivityComponent::class)   // Lives as long as Activity
object ActivityModule { }
```

## Testing

### Test with Hilt
```kotlin
@HiltAndroidTest
class MyTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var repository: MyRepository
    
    @Before
    fun init() {
        hiltRule.inject()
    }
    
    @Test
    fun myTest() {
        // Use repository
    }
}
```

### Replace Module for Testing
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object FakeNetworkModule {
    @Provides
    fun provideApiService(): ApiService = FakeApiService()
}

@UninstallModules(NetworkModule::class)
@HiltAndroidTest
class MyTest {
    // This test will use FakeNetworkModule instead
}
```

## Checklist for New Feature

- [ ] Create repository with `@Inject` constructor
- [ ] Add `@Singleton` scope if needed
- [ ] Create ViewModel with `@HiltViewModel` and `@Inject` constructor
- [ ] Use `hiltViewModel()` in Composable
- [ ] Ensure Activity has `@AndroidEntryPoint`
- [ ] Build project to generate Hilt components

## Troubleshooting

### ❌ Error: "cannot find symbol DaggerApplicationComponent"
**Solution:** Build the project first. Hilt generates code during compilation.

### ❌ Error: "Hilt does not support injection into private fields"
**Solution:** Make the field internal or public, or use constructor injection.

### ❌ Error: "android.app.Application does not have a @HiltAndroidApp annotated class"
**Solution:** Ensure `AnnotablyApplication` is referenced in `AndroidManifest.xml`.

### ❌ Error: "ViewModel not found"
**Solution:** 
1. Ensure ViewModel has `@HiltViewModel`
2. Ensure Activity has `@AndroidEntryPoint`
3. Use `hiltViewModel()` in Composable

### ❌ Error: "Cannot provide [Dependency]"
**Solution:** Add a `@Provides` or `@Binds` function in a Hilt module.

## Best Practices

✅ **DO:**
- Use constructor injection
- Use `@Binds` for interfaces (more efficient than `@Provides`)
- Keep modules focused and organized
- Use qualifiers instead of `@Named` for type safety

❌ **DON'T:**
- Use field injection in new code (prefer constructor)
- Create circular dependencies
- Put business logic in modules
- Inject Android framework types directly (use providers)

## Common Mistakes

### 1. Forgetting @InstallIn
```kotlin
// ❌ Wrong
@Module
object MyModule

// ✅ Correct
@Module
@InstallIn(SingletonComponent::class)
object MyModule
```

### 2. Missing @AndroidEntryPoint
```kotlin
// ❌ Wrong
class MainActivity : ComponentActivity()

// ✅ Correct
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

### 3. Not Using hiltViewModel()
```kotlin
// ❌ Wrong (for Hilt ViewModels)
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel())

// ✅ Correct
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel())
```

## Resources

- Full Guide: [DEPENDENCY_INJECTION.md](./DEPENDENCY_INJECTION.md)
- Implementation Details: [HILT_IMPLEMENTATION_SUMMARY.md](./HILT_IMPLEMENTATION_SUMMARY.md)
- Official Docs: https://developer.android.com/training/dependency-injection/hilt-android
