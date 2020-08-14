```java
package com.mastercard.cme.caas.web.security.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理 Cookie 工具
 */
public class CookieUtil {

    private final static String LOGIN_COOKIE = "LOGIN";

    /**
     * 根据 cookie 名获取对应 cookie，若不存在则返回 null
     *
     * @param request {@link HttpServletRequest}
     * @param name    cookie 的名字
     * @return 对应 cookie，若不存在则返回 null
     */
    public static Cookie get(HttpServletRequest request, String name) {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 为 response 加入 cookie，也可以用于删除 cookie，删除时 {@code maxAge} 的值为 0
     *
     * @param response {@link HttpServletResponse}
     * @param name     cookie 的名字
     * @param value    cookie 的值
     * @param maxAge   cookie 的过期时间
     */
    public static void set(HttpServletResponse response, String name, String value, Integer maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        if (maxAge != null) {
            cookie.setMaxAge(maxAge);
        }
        response.addCookie(cookie);
    }

    public static void addLoginCookie(HttpServletResponse response, String value) {
        set(response, LOGIN_COOKIE, value, null);
    }

    public static void deleteLoginCookie(HttpServletRequest request, HttpServletResponse response) {
        final Cookie cookie = get(request, LOGIN_COOKIE);
        if (cookie != null) {
            set(response, LOGIN_COOKIE, null, 0);
        }
    }
}
```