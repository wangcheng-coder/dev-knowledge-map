package mapdemo;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class GenericsErasure {
    static class Box<T> {}
    static class StringBox extends Box<String> {}

    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        strings.add("java");
        numbers.add(17);
        System.out.println("两个 List 的运行时类相同: " + (strings.getClass() == numbers.getClass()));
        System.out.println("运行时类名: " + strings.getClass().getName());
        ParameterizedType parent = (ParameterizedType) StringBox.class.getGenericSuperclass();
        System.out.println("字节码里保留的父类签名: " + parent.getTypeName());
        System.out.println("签名参数: " + parent.getActualTypeArguments()[0].getTypeName());
    }
}
