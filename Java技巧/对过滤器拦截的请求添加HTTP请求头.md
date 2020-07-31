# 对过滤器拦截的请求添加HTTP请求头

在过滤器中，起到过滤作用的方法一般有三个入参：
- 请求
- 响应
- 过滤器链

例如：
```java
 protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
```

那么如果需要对被拦截下来的 HttpServletRequest request 再添加上 HTTP 请求头，则需要
创建自己的 RequestWrapper 类，继承 HttpServletRequestWrapper。

参考以下代码：
过滤器是 spring security 的过滤器，不过道理相同。

```java
public class GiveTokenForTestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final MyRequestWrapper myRequestWrapper = new MyRequestWrapper(request);
        myRequestWrapper.addHeader("Authorization", "token");

        filterChain.doFilter(myRequestWrapper, response);
    }
}

class MyRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, String> headers = new HashMap<>();

    public MyRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        return headers.containsKey(name) ? headers.get(name) : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        names.addAll(headers.keySet());
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = Collections.list(super.getHeaders(name));
        if (headers.containsKey(name)) {
            values.add(headers.get(name));
        }
        return Collections.enumeration(values);
    }
}
```