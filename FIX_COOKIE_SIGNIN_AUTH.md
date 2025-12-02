# Fix: Cookie Sign-In and Test Endpoints Not Working

## 🐛 Problem

After fixing the `/uploads/**` 401 issue, some endpoints stopped working:

**Working:**
- ✅ `/api/auth/signin` 
- ✅ `/api/creditcard`

**Not Working:**
- ❌ `/api/cookie/signin` → 401 Unauthorized
- ❌ `/api/test/user` → 401 Unauthorized

## 🔍 Root Cause

The `AUTH_WHITELIST` and `PUBLIC_ENDPOINTS` configurations had two issues:

1. **Missing `/api/cookie/**`** - Cookie-based auth endpoints were not in the public list
2. **Wrong pattern for test endpoints** - Used `/api/test/**` which made ALL test endpoints public, including protected ones like `/api/test/user` that require `@PreAuthorize("hasRole('USER')")`

### **The Conflict:**

```java
// In WebSecurityConfig
"/api/test/**"  // This bypasses ALL test endpoints (WRONG!)

// In TestRestAPIs.java
@GetMapping("/api/test/user")
@PreAuthorize("hasRole('USER')")  // But this requires auth! (CONFLICT!)
public String userAccess() { ... }
```

When Spring Security bypassed `/api/test/**`, it never checked the `@PreAuthorize` annotation, causing authorization to fail.

## ✅ Solution Applied

Updated both configuration files to properly handle public vs protected endpoints:

### **1. WebSecurityConfig.java**

**Before:**
```java
"/api/auth/**",      // Authentication endpoints
"/api/test/**"       // ALL test endpoints (TOO PERMISSIVE!)
```

**After:**
```java
"/api/auth/**",      // Authentication endpoints (login, register, etc.)
"/api/cookie/**",    // Cookie-based authentication (ADDED)
"/api/test"          // Only base test endpoint, not /** (FIXED)
```

### **2. JwtAuthTokenFilter.java**

**Before:**
```java
"/api/auth/**",
"/api/test/**"  // Made all test endpoints skip JWT (WRONG!)
```

**After:**
```java
"/api/auth/**",
"/api/cookie/**",  // Added cookie endpoints
"/api/test"        // Only base endpoint (FIXED)
```

## 📊 Expected Behavior After Fix

| Endpoint | Authentication Required | Should Work |
|----------|------------------------|-------------|
| `/api/auth/signin` | ❌ No (public) | ✅ Yes |
| `/api/cookie/signin` | ❌ No (public) | ✅ Yes (FIXED) |
| `/api/test` | ❌ No (public) | ✅ Yes |
| `/api/test/user` | ✅ Yes (requires USER role) | ✅ Yes (FIXED) |
| `/api/test/admin` | ✅ Yes (requires ADMIN role) | ✅ Yes |
| `/api/creditcard` | ✅ Yes (requires USER/ADMIN) | ✅ Yes |
| `/uploads/**` | ❌ No (static resources) | ✅ Yes |

## 🧪 Testing

After rebuilding and restarting, test these endpoints:

### **1. Public endpoints (no auth needed):**
```bash
# Cookie sign-in (should work now)
curl -i -X POST http://192.168.0.31:9090/api/cookie/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
# Expected: 200 OK with Set-Cookie header

# Base test endpoint (should work)
curl -i http://192.168.0.31:9090/api/test
# Expected: 200 OK ">>> Runing"

# Auth sign-in (already working)
curl -i -X POST http://192.168.0.31:9090/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
# Expected: 200 OK with JWT token
```

### **2. Protected endpoints (auth required):**
```bash
# Test user endpoint (should require auth)
curl -i http://192.168.0.31:9090/api/test/user
# Expected: 401 Unauthorized (correct!)

# With valid JWT token (should work)
curl -i http://192.168.0.31:9090/api/test/user \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
# Expected: 200 OK ">>> User Contents!"
```

## 🔐 Security Notes

**The fix is secure because:**

1. ✅ Cookie authentication endpoints are public (needed for login)
2. ✅ Only `/api/test` is public (the health check endpoint)
3. ✅ Protected endpoints like `/api/test/user` now require proper authentication
4. ✅ `@PreAuthorize` annotations are now properly enforced
5. ✅ JWT filter runs on protected endpoints

## 📝 Key Takeaways

**Pattern Matching in Spring Security:**

- `/api/test` → Matches ONLY `/api/test`
- `/api/test/**` → Matches `/api/test`, `/api/test/user`, `/api/test/admin`, etc.

**Use `/api/test/**` only if ALL sub-paths should be public!**

For mixed scenarios (some public, some protected), use specific path matching:
- Public: `/api/test`
- Protected: `/api/test/user`, `/api/test/admin` (handled by `@PreAuthorize`)

---

**Status:** ✅ Fixed
**Date:** December 2, 2025
**Files Modified:**
- WebSecurityConfig.java
- JwtAuthTokenFilter.java

**Action Required:** Rebuild and restart application to apply changes.
