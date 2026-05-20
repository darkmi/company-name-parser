package com.darkmi.parser;

import java.util.List;

public class CompanyName {
    private String place;
    private String brand;
    private String trade;
    private String suffix;
    private String symbol;

    private List<Token> placeTokens;
    private List<Token> brandTokens;
    private List<Token> tradeTokens;
    private List<Token> suffixTokens;
    private List<Token> symbolTokens;

    public CompanyName() {
    }

    public CompanyName(String place, String brand, String trade, String suffix, String symbol) {
        this.place = place;
        this.brand = brand;
        this.trade = trade;
        this.suffix = suffix;
        this.symbol = symbol;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<Token> getPlaceTokens() {
        return placeTokens;
    }

    public void setPlaceTokens(List<Token> placeTokens) {
        this.placeTokens = placeTokens;
    }

    public List<Token> getBrandTokens() {
        return brandTokens;
    }

    public void setBrandTokens(List<Token> brandTokens) {
        this.brandTokens = brandTokens;
    }

    public List<Token> getTradeTokens() {
        return tradeTokens;
    }

    public void setTradeTokens(List<Token> tradeTokens) {
        this.tradeTokens = tradeTokens;
    }

    public List<Token> getSuffixTokens() {
        return suffixTokens;
    }

    public void setSuffixTokens(List<Token> suffixTokens) {
        this.suffixTokens = suffixTokens;
    }

    public List<Token> getSymbolTokens() {
        return symbolTokens;
    }

    public void setSymbolTokens(List<Token> symbolTokens) {
        this.symbolTokens = symbolTokens;
    }

    @Override
    public String toString() {
        return "CompanyName{" +
                "place='" + place + '\'' +
                ", brand='" + brand + '\'' +
                ", trade='" + trade + '\'' +
                ", suffix='" + suffix + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}