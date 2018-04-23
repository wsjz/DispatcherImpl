package controllers;

import annotation.MyController;
import annotation.MyRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Created by Darling
 * @version CreatedDate: 2018/4/23 at 18:59
 */
@MyController
@MyRequestMapping("/test")
public class TestController {
    @MyRequestMapping("/doTest")
    public void test(HttpServletRequest request, HttpServletResponse response){
        System.out.println("jz");
        try {
            response.getWriter().write("hello");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
