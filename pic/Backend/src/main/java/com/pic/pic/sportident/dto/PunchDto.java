package com.pic.pic.sportident.dto;

public record PunchDto(
    long id,
    String card,
    int code,
    String modem,
    long time
) {}
