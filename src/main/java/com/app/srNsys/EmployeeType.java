package com.app.srNsys;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum EmployeeType {
    ODC1(new HashSet<>(Arrays.asList("李优","冯辉", "刘畅", "葛茂盛","庞瑞","方迪","陈全新","王有伦","范喜悦"))),
    ODC2(new HashSet<>(Arrays.asList("王俊杰","李美静","焦龙"))),
    OTHER(new HashSet<>(Arrays.asList("初科翰","刘文峰","王阳","李金城1","李金城","guest1","魏梦","季雪侨","保洁","柳俊明","临时卡","李松烛","方元","朱兴隆")));

    private final Set<String> names;

    EmployeeType(Set<String> names) {
        this.names = names;
    }

    public static boolean isValidEmployee(String employeeName) {
        return ODC1.getNames().contains(employeeName) || ODC2.getNames().contains(employeeName)|| OTHER.getNames().contains(employeeName);
    }
}

