package servlet;

import annotation.MyController;
import annotation.MyRequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author Created by Darling
 * @version CreatedDate: 2018/4/23 at 14:42
 */

public class MyDispatcherServlet extends HttpServlet {
    private Properties properties = new Properties();
    private List<String> classNames = new ArrayList<>();
    private Map<String, Object> ioc = new HashMap<>();//ioc容器模拟
    private Map<String, Method> handlerMapping = new HashMap<>();
    private Map<String, Object> controllerMapping = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        doLoadConfig(config.getInitParameter("mvcConfig"));
        doScanner(properties.getProperty("scanPackage"));
        doInstance();//实例化控制器放入IOC容器
        initHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Server Exception");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if(handlerMapping.isEmpty()) {
            return;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        //取相对路径
        url = url.replace(contextPath,"").replaceAll("/+","/");
        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found");
            return;
        }
        //获取映射的方法
        Method method = this.handlerMapping.get(url);
        //获取controller方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        //保存参数值
        Object[] paramValues = new Object[parameterTypes.length];
        for(int i = 0; i < parameterTypes.length; i++){
            String requestParam = parameterTypes[i].getSimpleName();
            if(requestParam.equals("HttpServletRequest")) {
                paramValues[i] = req;
                continue;
            }
            if(requestParam.equals("HttpServletResponse")) {
                paramValues[i] = resp;
                continue;
            }
            if (requestParam.equals("String")) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue())
                            .replace("\\[|\\]", "")
                            .replaceAll(",\\s", ",");
                    paramValues[i] = value;
                }
            }
        }
        method.invoke(this.controllerMapping.get(url),paramValues);
    }


    //加载配置
    private void doLoadConfig(String location) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //扫描包
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.","/"));
        File dir = new File(url.getFile());
        for(File file : dir.listFiles()) {
            if(file.isDirectory()) {
                //递归从目录读取包
                doScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replaceAll(".class","");
                classNames.add(className);
            }
        }
    }

    //实例化Controller
    private void doInstance() {
        if(classNames.isEmpty()) return;
        for (String className : classNames) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
                //只有带MyController注解的类才需要实例化放入ioc容器
                if(clazz.isAnnotationPresent(MyController.class)){
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()),clazz.newInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }


    //初始化处理器映射器
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        //从已加载的Bean中找到控制器
        for (Map.Entry entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }

            String baseUrl = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = annotation.value();
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods){
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                String url = annotation.value();
                url = (baseUrl + "/" + url).replaceAll("/+","/");
                handlerMapping.put(url,method);
                try {
                    controllerMapping.put(url,clazz.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String toLowerFirstWord(String name) {
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }

}
