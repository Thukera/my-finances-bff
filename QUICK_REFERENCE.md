# Quick Reference Guide - Refactored Architecture

## 🚀 Quick Start

### **Creating a New Feature?** Follow this pattern:

```
1. Create Service
2. Create Mapper (if needed)
3. Create/Update Controller
4. Use AuthenticationHelper for auth
5. Let GlobalExceptionHandler handle errors
```

---

## 📋 Common Patterns

### **Pattern 1: Creating a Resource**

**Service:**
```java
@Service
public class YourService {
    @Autowired
    private YourRepository repository;
    
    @Autowired
    private YourMapper mapper;
    
    @Autowired
    private AuthenticationHelper authHelper;
    
    @Transactional
    public YourDTO create(YourForm form) {
        User user = authHelper.getCurrentUser();
        YourEntity entity = mapper.toEntity(form, user);
        YourEntity saved = repository.save(entity);
        return mapper.toDTO(saved);
    }
}
```

**Controller:**
```java
@RestController
@RequestMapping("/api/your-resource")
public class YourController {
    @Autowired
    private YourService service;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<YourDTO> create(@RequestBody YourForm form) {
        YourDTO result = service.create(form);
        return ResponseEntity.ok(result);
    }
}
```

---

### **Pattern 2: Getting a Resource with Ownership Validation**

**Service:**
```java
@Transactional(readOnly = true)
public YourDTO getById(Long id) {
    YourEntity entity = repository.findById(id)
        .orElseThrow(() -> new NotFoundException("Not found"));
    
    // Validate ownership
    if (!authHelper.canAccessUserResource(entity.getUser().getId())) {
        throw new SecurityException("Access denied");
    }
    
    return mapper.toDTO(entity);
}
```

**Controller:**
```java
@GetMapping("/{id}")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<YourDTO> getById(@PathVariable Long id) {
    YourDTO result = service.getById(id);
    return ResponseEntity.ok(result);
}
```

---

### **Pattern 3: Updating a Resource**

**Service:**
```java
@Transactional
public YourDTO update(Long id, YourForm form) {
    YourEntity entity = repository.findById(id)
        .orElseThrow(() -> new NotFoundException("Not found"));
    
    // Validate ownership
    if (!authHelper.canAccessUserResource(entity.getUser().getId())) {
        throw new SecurityException("Access denied");
    }
    
    mapper.updateEntity(entity, form);
    YourEntity updated = repository.save(entity);
    return mapper.toDTO(updated);
}
```

**Controller:**
```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<YourDTO> update(@PathVariable Long id, 
                                       @RequestBody YourForm form) {
    YourDTO result = service.update(id, form);
    return ResponseEntity.ok(result);
}
```

---

## 🔧 Helper Service Usage

### **AuthenticationHelper Methods:**

```java
// Get current authenticated user
User user = authHelper.getCurrentUser();

// Get current username
String username = authHelper.getCurrentUsername();

// Check if current user is admin
boolean isAdmin = authHelper.isCurrentUserAdmin();

// Check if user can access a resource
boolean canAccess = authHelper.canAccessUserResource(resourceOwnerId);

// Get authentication object
Authentication auth = authHelper.getCurrentAuthentication();
```

---

## 🎨 Mapper Pattern

### **Basic Mapper:**

```java
@Component
public class YourMapper {
    
    // Form → Entity
    public YourEntity toEntity(YourForm form, User user) {
        YourEntity entity = new YourEntity();
        entity.setUser(user);
        entity.setField1(form.getField1());
        entity.setField2(form.getField2());
        return entity;
    }
    
    // Entity → DTO
    public YourDTO toDTO(YourEntity entity) {
        return new YourDTO(
            entity.getId(),
            entity.getField1(),
            entity.getField2(),
            entity.getCreatedAt()
        );
    }
    
    // Update existing entity
    public void updateEntity(YourEntity entity, YourForm form) {
        if (form.getField1() != null) {
            entity.setField1(form.getField1());
        }
        if (form.getField2() != null) {
            entity.setField2(form.getField2());
        }
    }
}
```

---

## ⚠️ Exception Handling

### **Don't do this in controllers:**
```java
❌ try { ... } catch (Exception e) { ... }
```

### **Do this instead:**
```java
✅ Just throw the exception - GlobalExceptionHandler will catch it!

throw new NotFoundException("Resource not found");
throw new SecurityException("Access denied");
throw new IllegalArgumentException("Invalid data");
```

### **Available Exceptions:**
- `NotFoundException` → 404
- `SecurityException` → 403
- `IllegalArgumentException` → 400
- `TransactionSystemException` → 400
- `Exception` → 500

---

## 📦 Layer Responsibilities

| Layer | Responsibility | Example |
|-------|---------------|---------|
| **Controller** | HTTP handling, routing | `@PostMapping`, `@GetMapping` |
| **Service** | Business logic | Validation, orchestration |
| **Mapper** | Transformations | Entity ↔ DTO |
| **Repository** | Database access | `findById()`, `save()` |
| **Helper** | Shared utilities | `getCurrentUser()` |

---

## ✅ Checklist for New Endpoints

When creating a new endpoint:

- [ ] Business logic in **Service**, not Controller
- [ ] Use **AuthenticationHelper** for auth
- [ ] Use **Mapper** for transformations
- [ ] **Don't** add try-catch in Controller
- [ ] Add **@PreAuthorize** annotation
- [ ] Add **@Transactional** on Service methods
- [ ] Throw exceptions for errors
- [ ] Return proper HTTP status codes
- [ ] Add logging with logger.debug()

---

## 🎯 Don'ts (Anti-Patterns)

❌ **Don't** put business logic in controllers  
❌ **Don't** inject repositories in controllers  
❌ **Don't** use SecurityContextHolder in controllers  
❌ **Don't** add try-catch blocks in controllers  
❌ **Don't** create entities in controllers  
❌ **Don't** duplicate authentication code  
❌ **Don't** put mapping logic in Form/DTO classes  

---

## ✅ Do's (Best Practices)

✅ **Do** delegate to services  
✅ **Do** use AuthenticationHelper  
✅ **Do** use Mappers for transformations  
✅ **Do** throw exceptions  
✅ **Do** add @Transactional on service methods  
✅ **Do** validate ownership in services  
✅ **Do** keep controllers thin  
✅ **Do** log important operations  

---

## 🧪 Testing Pattern

### **Service Test:**
```java
@ExtendWith(MockitoExtension.class)
class YourServiceTest {
    
    @Mock
    private YourRepository repository;
    
    @Mock
    private YourMapper mapper;
    
    @Mock
    private AuthenticationHelper authHelper;
    
    @InjectMocks
    private YourService service;
    
    @Test
    void testCreate() {
        // Given
        when(authHelper.getCurrentUser()).thenReturn(mockUser);
        when(mapper.toEntity(any(), any())).thenReturn(mockEntity);
        when(repository.save(any())).thenReturn(savedEntity);
        when(mapper.toDTO(any())).thenReturn(mockDTO);
        
        // When
        YourDTO result = service.create(form);
        
        // Then
        assertNotNull(result);
        verify(repository).save(any());
    }
}
```

---

## 📚 File Locations

```
src/main/java/com/thukera/
├── [feature]/
│   ├── controller/        ← Thin controllers
│   ├── service/           ← Business logic
│   ├── mapper/            ← Transformations
│   ├── repository/        ← Data access
│   └── model/
│       ├── entities/      ← JPA entities
│       ├── dto/           ← Response objects
│       └── form/          ← Request objects
```

---

## 🔗 Quick Links

- **REFACTORING_SUMMARY.md** - Detailed explanation
- **REFACTORING_COMPARISON.md** - Before/after comparison
- **ARCHITECTURE_DIAGRAM.md** - Architecture overview
- **REFACTORING_COMPLETE.md** - Summary report

---

## 💡 Pro Tips

1. **Service methods should be transactional**
   ```java
   @Transactional // for write operations
   @Transactional(readOnly = true) // for read operations
   ```

2. **Log important operations**
   ```java
   logger.debug("### Creating resource: {}", form);
   logger.error("### Error occurred", exception);
   ```

3. **Use meaningful exception messages**
   ```java
   throw new NotFoundException("Credit card not found with ID: " + id);
   ```

4. **Validate ownership**
   ```java
   if (!authHelper.canAccessUserResource(ownerId)) {
       throw new SecurityException("Access denied");
   }
   ```

5. **Keep methods small and focused**
   - One method = one responsibility
   - Extract complex logic into private methods

---

**Keep this guide handy when developing new features!** 🚀
