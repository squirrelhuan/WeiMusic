package com.demomaster.weimusic;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test(){
        Pattern pattern = Pattern.compile("Content-Type:(.*)\n");
        Matcher matcher = pattern.matcher("124Content-Type: audio/mpeg\n");
        if (matcher.find()) {
            System.out.println("size="+matcher.group().length());
            //获取数据的大小
           for(int i=0;i<matcher.group().length();i++) {
               System.out.println(i+"="+matcher.group(i));
           }
        }
    }
}