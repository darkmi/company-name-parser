package com.darkmi.parser;

public class ParserTest {
    public static void main(String[] args) {
        CompanyNameParser parser = new CompanyNameParser();

        String[] names = {
            "武汉海明智业电子商务有限公司",
            "泉州益念食品有限公司",
            "常州途畅互联网科技有限公司合肥分公司",
            "昆明享亚教育信息咨询有限公司"
        };

        for (String name : names) {
            CompanyName cn = parser.parse(name);
            System.out.println("Input: " + name);
            System.out.println("  place: " + cn.getPlace());
            System.out.println("  brand: " + cn.getBrand());
            System.out.println("  trade: " + cn.getTrade());
            System.out.println("  suffix: " + cn.getSuffix());
            System.out.println();
        }
    }
}